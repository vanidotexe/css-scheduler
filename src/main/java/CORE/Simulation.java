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

public class Simulation {

    private final SimulationConfig config;

    private final CPU[] cpus;
    private final Memory memory;
    private final Disk disk;

    private final ReadyQueue readyQueue = new ReadyQueue();
    private final SyscallQueue pendingSyscalls = new SyscallQueue();
    private final EventQueue events = new EventQueue();
    private final SystemProcess sysProc = new SystemProcess();

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

    public void run() {
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

            dispatch();

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

    private void onProcessRelease(Event.ProcessRelease e) {
        UserProcess p = e.process;
        assert p.getState() == ProcessState.NEW;
        p.setState(ProcessState.READY);
        readyQueue.addLast(p);
    }

    private void onSysRelease(Event.SysRelease e) {
        if (terminatedCount < config.processes.size() || !pendingSyscalls.isEmpty()) {
            events.add(new Event.SysRelease(clock + config.sysPeriod, events.nextSeq()));
        }

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

        logger.record(p.getLastDispatchTime(), clock, "CPU" + cpu.id, Logger.Kind.USER_BURST,
                p.id, "burst#" + p.getCurrentBurstIdx());

        p.consumeBurstTime(ran);
        cpu.release();

        if (p.getRemainingInBurst() == 0) {
            if (p.isOnLastBurst()) {
                p.setState(ProcessState.TERMINATED);
                terminatedCount++;
                if (memory.isResident(p)) memory.evict(p);
            } else {
                int syscallDur = p.currentTrailingSyscall();
                p.advanceToNextBurst();
                p.setState(ProcessState.BLOCKED);
                pendingSyscalls.addLast(new SystemProcess.Syscall(p, syscallDur));
                if (sysProc.getState() == SystemProcess.SysState.IDLE) {
                    sysProc.setState(SystemProcess.SysState.PENDING);
                }
            }
        } else {
            p.setState(ProcessState.READY);
            readyQueue.addLast(p);
        }
    }

    private void handleSysDispatchEnd(CPU cpu) {
        SystemProcess.Syscall done = sysProc.getCurrent();
        assert done != null;

        logger.record(cpu.getBusyUntil() - done.duration, clock, "CPU" + cpu.id,
                Logger.Kind.SYS_CALL, done.requester.id, "");

        UserProcess requester = done.requester;
        assert requester.getState() == ProcessState.BLOCKED;
        requester.setState(ProcessState.READY);
        readyQueue.addLast(requester);

        sysProc.setCurrent(null);
        cpu.release();

        if (!pendingSyscalls.isEmpty()) {
            SystemProcess.Syscall next = pendingSyscalls.removeFirst();
            sysProc.setCurrent(next);
            sysProc.setCpuId(cpu.id);
            sysProc.setState(SystemProcess.SysState.RUNNING);
            long endTime = clock + next.duration;
            cpu.assign(sysProc, endTime);
            events.add(new Event.DispatchEnd(endTime, events.nextSeq(), cpu.id));
        } else {
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
        } else {
            logger.record(clock - disk.computeDuration(t.process.memorySize), clock,
                    "DISK", Logger.Kind.SWAP_IN, t.process.id, "");
            UserProcess p = t.process;
            assert p.getState() == ProcessState.SWAPPING_IN;
            p.setState(ProcessState.READY);
            readyQueue.addLast(p);
        }
        Disk.Transfer head = disk.peekHead();
        if (head != null) {
            long dur = disk.computeDuration(head.process.memorySize);
            events.add(new Event.TransferDone(clock + dur, events.nextSeq(), head));
        }
    }

    private void dispatch() {
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
                sysProc.setState(SystemProcess.SysState.IDLE);
            }
        }

        while (!readyQueue.isEmpty() && hasIdleCpu()) {
            UserProcess p = readyQueue.removeFirst();
            boolean dispatched = tryDispatchUser(p);
            if (!dispatched) {
                readyQueue.addFirst(p);
                break;
            }
        }
    }

    private boolean tryDispatchUser(UserProcess p) {
        if (!memory.isResident(p)) {
            if (!makeRoomFor(p)) return false;
            memory.admit(p);
            p.setState(ProcessState.SWAPPING_IN);
            Disk.Transfer t = new Disk.Transfer(p, Disk.Direction.IN);
            boolean started = disk.enqueue(t);
            if (started) {
                long dur = disk.computeDuration(p.memorySize);
                events.add(new Event.TransferDone(clock + dur, events.nextSeq(), t));
            }
            return true;
        }

        CPU c = pickCpuForUser(p);
        if (c == null) {
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

    private boolean makeRoomFor(UserProcess p) {
        if (p.memorySize > memory.getCapacity()) {
            throw new SimulationException("Process P" + p.id + " requires more memory ("
                    + p.memorySize + ") than total RAM (" + memory.getCapacity() + ")");
        }
        if (memory.getFree() >= p.memorySize) return true;

        java.util.List<UserProcess> plan = memory.planEvictions(p.memorySize, this::isPinned);
        if (plan == null) return false;

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

    private boolean isPinned(UserProcess p) {
        return p.getState() == ProcessState.RUNNING
                || p.getState() == ProcessState.SWAPPING_IN;
    }

    private boolean hasIdleCpu() {
        for (CPU c : cpus) if (c.isIdle()) return true;
        return false;
    }

    private CPU pickIdleCpuForSys() {
        for (CPU c : cpus) if (c.isIdle()) return c;
        return null;
    }

    private CPU pickCpuForUser(UserProcess p) {
        int last = p.getLastCpuId();
        if (last >= 0 && cpus[last].isIdle()) return cpus[last];
        return pickIdleCpuForSys();
    }
}
