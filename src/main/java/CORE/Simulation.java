package CORE;

import HW.CPU;
import HW.Disk;
import HW.Memory;
import MODEL.ProcessState;
import MODEL.SystemProcess;
import MODEL.UserProcess;
import OUT.Logger;
import SCHED.Event;
import SCHED.EventQueue;
import SCHED.ReadyQueue;
import SCHED.SyscallQueue;
import UTILS.SimulationException;

/**
 * Discrete-event simulation of process scheduling, virtual memory, and a
 * periodic system process.
 *
 * Time advances by jumping to the next scheduled event. After each event is
 * handled, the scheduler is given a chance to dispatch any newly-eligible
 * tasks onto idle CPUs.
 */
public class Simulation {

    private final SimulationConfig config;

    // Hardware
    private final CPU[] cpus;
    private final Memory memory;
    private final Disk disk;

    // Scheduler state
    private final ReadyQueue readyQueue = new ReadyQueue();
    private final SyscallQueue pendingSyscalls = new SyscallQueue();
    private final EventQueue events = new EventQueue();
    private final SystemProcess sysProc = new SystemProcess();

    // Time and bookkeeping
    private long clock = 0;
    private final Logger logger = new Logger();
    private int terminatedCount = 0;

    public Simulation(SimulationConfig config) {
        this.config = config;
        this.cpus = new CPU[config.processors];
        for (int i = 0; i < config.processors; i++) cpus[i] = new CPU(i);
        this.memory = new Memory(config.ram);
        this.disk = new Disk(config.diskRate);
    }

    public Logger getLogger() { return logger; }
    public long getClock() { return clock; }

    // ====================================================================
    // Main loop
    // ====================================================================
    public void run() {
        // Seed initial events: each user process release, plus the first system release.
        for (UserProcess p : config.processes) {
            events.add(new Event.ProcessRelease(p.releaseTime, events.nextSeq(), p));
        }
        events.add(new Event.SysRelease(config.sysPeriod, events.nextSeq()));

        while (!events.isEmpty()) {
            Event e = events.poll();
            if (e.time < clock) {
                throw new SimulationException("Time travel: event at " + e.time + " < clock " + clock);
            }
            clock = e.time;

            if      (e instanceof Event.ProcessRelease) onProcessRelease((Event.ProcessRelease) e);
            else if (e instanceof Event.SysRelease)     onSysRelease((Event.SysRelease) e);
            else if (e instanceof Event.DispatchEnd)    onDispatchEnd((Event.DispatchEnd) e);
            else if (e instanceof Event.TransferDone)   onTransferDone((Event.TransferDone) e);
            else throw new SimulationException("Unknown event type: " + e.getClass());

            // After every event, try to dispatch.
            dispatch();

            // Termination guard: if all user processes have terminated and there is
            // nothing else outstanding, drop any remaining periodic SysReleases so we exit.
            if (terminatedCount == config.processes.size()
                    && pendingSyscalls.isEmpty()
                    && sysProc.getState() == SystemProcess.SysState.IDLE
                    && allCpusIdle()
                    && disk.isEmpty()) {
                break;
            }
        }
    }

    private boolean allCpusIdle() {
        for (CPU c : cpus) if (!c.isIdle()) return false;
        return true;
    }

    // ====================================================================
    // Event handlers
    // ====================================================================
    private void onProcessRelease(Event.ProcessRelease e) {
        UserProcess p = e.process;
        assert p.getState() == ProcessState.NEW;
        p.setState(ProcessState.READY);
        readyQueue.addLast(p);
    }

    private void onSysRelease(Event.SysRelease e) {
        // Schedule the next periodic release. We always schedule it; the run loop
        // will exit naturally if everything else has terminated.
        if (terminatedCount < config.processes.size() || !pendingSyscalls.isEmpty()) {
            events.add(new Event.SysRelease(clock + config.sysPeriod, events.nextSeq()));
        }

        // If there is pending syscall work and the system process is currently idle,
        // mark it pending so the next dispatch pass picks it up.
        if (sysProc.getState() == SystemProcess.SysState.IDLE && !pendingSyscalls.isEmpty()) {
            sysProc.setState(SystemProcess.SysState.PENDING);
        }
    }

    private void onDispatchEnd(Event.DispatchEnd e) {
        CPU cpu = cpus[e.cpuId];
        Object running = cpu.getCurrent();
        assert running != null : "DispatchEnd on an idle CPU " + e.cpuId;

        if (running instanceof UserProcess) {
            handleUserDispatchEnd(cpu, (UserProcess) running);
        } else if (running instanceof SystemProcess) {
            handleSysDispatchEnd(cpu);
        } else {
            throw new SimulationException("Unexpected task on CPU: " + running.getClass());
        }
    }

    private void handleUserDispatchEnd(CPU cpu, UserProcess p) {
        long ran = clock - p.getLastDispatchTime();
        assert ran > 0 : "Zero-time dispatch for P" + p.id;

        // Log the burst execution interval
        logger.record(p.getLastDispatchTime(), clock, "CPU" + cpu.id, Logger.Kind.USER_BURST,
                p.id, "burst#" + p.getCurrentBurstIdx());

        p.consumeBurstTime(ran);
        cpu.release();

        if (p.getRemainingInBurst() == 0) {
            // Burst finished. Either there is a trailing syscall, or the process terminates.
            if (p.isOnLastBurst()) {
                p.setState(ProcessState.TERMINATED);
                terminatedCount++;
                // Release the process's RAM so it can be used by others.
                if (memory.isResident(p)) memory.evict(p);
            } else {
                int syscallDur = p.currentTrailingSyscall();
                p.advanceToNextBurst();
                p.setState(ProcessState.BLOCKED);
                pendingSyscalls.addLast(new SystemProcess.Syscall(p, syscallDur));
                // If system process is idle, mark it pending so it can run.
                if (sysProc.getState() == SystemProcess.SysState.IDLE) {
                    sysProc.setState(SystemProcess.SysState.PENDING);
                }
            }
        } else {
            // Slice expired but burst still has time left -> requeue.
            p.setState(ProcessState.READY);
            readyQueue.addLast(p);
        }
    }

    private void handleSysDispatchEnd(CPU cpu) {
        SystemProcess.Syscall done = sysProc.getCurrent();
        assert done != null;

        logger.record(cpu.getBusyUntil() - done.duration, clock, "CPU" + cpu.id,
                Logger.Kind.SYS_CALL, done.requester.id, "");

        // Unblock the requester
        UserProcess requester = done.requester;
        assert requester.getState() == ProcessState.BLOCKED;
        requester.setState(ProcessState.READY);
        readyQueue.addLast(requester);

        sysProc.setCurrent(null);
        cpu.release();

        if (!pendingSyscalls.isEmpty()) {
            // Still work to do: pick the next syscall and continue executing on the same CPU.
            SystemProcess.Syscall next = pendingSyscalls.removeFirst();
            sysProc.setCurrent(next);
            sysProc.setCpuId(cpu.id);
            sysProc.setState(SystemProcess.SysState.RUNNING);
            long endTime = clock + next.duration;
            cpu.assign(sysProc, endTime);
            events.add(new Event.DispatchEnd(endTime, events.nextSeq(), cpu.id));
        } else {
            // Instance done.
            sysProc.setState(SystemProcess.SysState.IDLE);
            sysProc.setCpuId(-1);
        }
    }

    private void onTransferDone(Event.TransferDone e) {
        Disk.Transfer t = disk.completeHead();
        assert t == e.transfer;

        if (t.direction == Disk.Direction.OUT) {
            logger.record(clock - disk.computeDuration(t.process.memorySize), clock,
                    "DISK", Logger.Kind.SWAP_OUT, t.process.id, "");
            // The process has been written out; memory was already freed when we scheduled
            // the eviction. Nothing more to do.
        } else {
            // SWAP_IN: process is now resident.
            logger.record(clock - disk.computeDuration(t.process.memorySize), clock,
                    "DISK", Logger.Kind.SWAP_IN, t.process.id, "");
            UserProcess p = t.process;
            assert p.getState() == ProcessState.SWAPPING_IN;
            // Memory.admit was called when we scheduled the transfer (we reserved space).
            // Now mark the process as ready so it will be redispatched.
            p.setState(ProcessState.READY);
            readyQueue.addLast(p);
        }

        // Start next transfer if any are queued.
        Disk.Transfer head = disk.peekHead();
        if (head != null) {
            long dur = disk.computeDuration(head.process.memorySize);
            events.add(new Event.TransferDone(clock + dur, events.nextSeq(), head));
        }
    }

    // ====================================================================
    // Dispatch
    // ====================================================================
    /**
     * Try to assign tasks to idle CPUs. Called after every event handler.
     *
     * Order:
     *   1. The system process gets first pick (one CPU only) if it's PENDING.
     *   2. Then user processes are pulled from the ready queue, applying CPU affinity.
     *
     * If a chosen user process is not in RAM, this triggers swap-in machinery
     * instead of an immediate dispatch.
     */
    private void dispatch() {
        // Step 1: system process (waits for free CPU)
        if (sysProc.getState() == SystemProcess.SysState.PENDING) {
            CPU c = pickIdleCpuForSys();
            if (c != null && !pendingSyscalls.isEmpty()) {
                SystemProcess.Syscall next = pendingSyscalls.removeFirst();
                sysProc.setCurrent(next);
                sysProc.setCpuId(c.id);
                sysProc.setState(SystemProcess.SysState.RUNNING);
                long endTime = clock + next.duration;
                c.assign(sysProc, endTime);
                events.add(new Event.DispatchEnd(endTime, events.nextSeq(), c.id));
            } else if (c != null) {
                // No actual work; revert to idle.
                sysProc.setState(SystemProcess.SysState.IDLE);
            }
        }

        // Step 2: user processes
        while (!readyQueue.isEmpty() && hasIdleCpu()) {
            UserProcess p = readyQueue.removeFirst();
            boolean dispatched = tryDispatchUser(p);
            if (!dispatched) {
                // We couldn't dispatch this one (RAM exhausted). Put it back at the head
                // and stop trying further; the next event (transfer done, syscall done,
                // process terminate, ...) will free memory and re-trigger dispatch.
                readyQueue.addFirst(p);
                break;
            }
        }
    }

    /**
     * Try to dispatch user process p. Returns true if it ended up either running
     * on a CPU (resident path) or with a swap-in transfer initiated (non-resident
     * path). Returns false only when memory cannot be made room for p right now,
     * in which case the caller is expected to put p back on the ready queue.
     */
    private boolean tryDispatchUser(UserProcess p) {
        // Memory residency check
        if (!memory.isResident(p)) {
            if (!makeRoomFor(p)) return false;     // caller will requeue
            // We have space now. Reserve memory by admitting and start the swap-in.
            memory.admit(p);
            p.setState(ProcessState.SWAPPING_IN);
            Disk.Transfer t = new Disk.Transfer(p, Disk.Direction.IN);
            boolean started = disk.enqueue(t);
            if (started) {
                long dur = disk.computeDuration(p.memorySize);
                events.add(new Event.TransferDone(clock + dur, events.nextSeq(), t));
            }
            return true;     // process will be re-readied when transfer completes
        }

        // Resident: pick a CPU using affinity, then dispatch.
        CPU c = pickCpuForUser(p);
        if (c == null) {
            // No CPU free. Shouldn't happen given the dispatch loop's hasIdleCpu() guard,
            // but be defensive.
            return false;
        }

        memory.touch(p);
        p.setState(ProcessState.RUNNING);
        p.setLastCpuId(c.id);
        p.setLastDispatchTime(clock);

        long ranFor = Math.min(config.slice, p.getRemainingInBurst());
        long endTime = clock + ranFor;
        c.assign(p, endTime);
        events.add(new Event.DispatchEnd(endTime, events.nextSeq(), c.id));
        return true;
    }

    /**
     * Make sure there is at least p.memorySize bytes of free RAM. Uses a plan-
     * then-commit approach: first checks whether enough memory can be freed via
     * eviction; only if so, actually performs the evictions. Each eviction
     * enqueues a swap-out transfer on the disk. Returns true iff success.
     */
    private boolean makeRoomFor(UserProcess p) {
        if (p.memorySize > memory.getCapacity()) {
            throw new SimulationException("Process P" + p.id + " requires more memory ("
                    + p.memorySize + ") than total RAM (" + memory.getCapacity() + ")");
        }
        if (memory.getFree() >= p.memorySize) return true;

        java.util.List<UserProcess> plan = memory.planEvictions(p.memorySize, this::isPinned);
        if (plan == null) return false;

        // Commit: evict each victim and enqueue swap-out transfers in plan order.
        for (UserProcess victim : plan) {
            memory.evict(victim);
            Disk.Transfer t = new Disk.Transfer(victim, Disk.Direction.OUT);
            boolean started = disk.enqueue(t);
            if (started) {
                long dur = disk.computeDuration(victim.memorySize);
                events.add(new Event.TransferDone(clock + dur, events.nextSeq(), t));
            }
        }
        return true;
    }

    /**
     * A process is pinned (cannot be evicted) if it's currently running on a CPU
     * or actively being swapped in. All other states (READY, BLOCKED, TERMINATED)
     * are eligible for LRU eviction.
     */
    private boolean isPinned(UserProcess p) {
        return p.getState() == ProcessState.RUNNING
                || p.getState() == ProcessState.SWAPPING_IN;
    }

    private boolean hasIdleCpu() {
        for (CPU c : cpus) if (c.isIdle()) return true;
        return false;
    }

    private CPU pickIdleCpuForSys() {
        // Lowest-id idle CPU.
        for (CPU c : cpus) if (c.isIdle()) return c;
        return null;
    }

    /**
     * Affinity rule: if multiple CPUs are idle, pick the one this process ran on
     * most recently. Otherwise (no prior history, or that CPU is busy), pick the
     * lowest-id idle CPU.
     */
    private CPU pickCpuForUser(UserProcess p) {
        int last = p.getLastCpuId();
        if (last >= 0 && cpus[last].isIdle()) return cpus[last];
        return pickIdleCpuForSys();
    }
}
