package com.marginallyclever.evilOverlord.communications;





public interface MarginallyCleverConnectionReadyListener {
	public void serialConnectionReady(MarginallyCleverConnection arg0);
	public void serialDataAvailable(MarginallyCleverConnection arg0,String data);
}
