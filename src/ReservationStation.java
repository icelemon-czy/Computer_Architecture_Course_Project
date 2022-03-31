/**
 *  *  The architecture has the following functional units with the shown latencies and number of reservation stations.
 *  *  Unit       |    Latency(cycle) for op           | Reservation Stations          | Instructions executing on the unit
 *  *  INT          1 (integer and logic operations)            4                              add / addi
 *  *  Load/Store   1 for address calculation            2 load buffer + 2 store buffer        fld fsd
 *  *  FPadd        3 (non-pipelined FP add)                    3                              fadd / fsub
 *  *  FPmult       4 (non-pipelined FP multiply)               4                              fmul
 *  *  FPdiv        8 (non-pipelined FP divide)                 2                              fdiv
 *  *  BU           1 (condition and target evaluation)         1                              bne
 */
public class ReservationStation {

}
