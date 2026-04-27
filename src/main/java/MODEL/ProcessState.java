package MODEL;

/**
 * Lifecycle states for a user process.
 *
 *  NEW         -> not yet released (release time in the future)
 *  READY       -> released, in RAM (or eligible to be brought to RAM), waiting for a CPU
 *  RUNNING     -> currently executing on a CPU
 *  BLOCKED     -> waiting for a system call to be served by the system process
 *  SWAPPING_IN -> chosen by the scheduler but not yet resident; disk transfer in progress
 *  TERMINATED  -> finished its last burst
 */
public enum ProcessState {
    NEW,
    READY,
    RUNNING,
    BLOCKED,
    SWAPPING_IN,
    TERMINATED
}
