

package manager;


public class ConfirmCommitCountDownThread extends Thread {
    int timeout;
    Vapp vapp;
    VpnFrame vpnFrame;
    public boolean closedSocketAborted = false;

    ConfirmCommitCountDownThread(int timeout, Vapp vapp, VpnFrame vpnFrame) {
	this.timeout = timeout;
	this.vapp = vapp;
	this.vpnFrame = vpnFrame;
    }
    
    //@Override
    public void run() {
	while (true) {
	    if (timeout == 0 || closedSocketAborted) {
		vpnFrame.hideConfirmCommitButtons();
		if (!closedSocketAborted)
		    vapp.timedOutCommit();
		return;
	    }
	    
	    try {
		sleep(1000);
	    } catch (InterruptedException e) {
		vpnFrame.hideConfirmCommitButtons();
		return;
	    }
	    
	    vpnFrame.getAckButton().setText("Ack ("+timeout+")");
	    timeout--;
	}
    }
}
