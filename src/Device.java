/**
 * Created by natefuller on 4/17/15.
 */
public class Device {

    public Process currentProcess;

    public boolean isIdle() {
        return currentProcess == null;
    }

}
