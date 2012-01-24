package org.alienlabs.multirequest;

import java.util.concurrent.Callable;

public class Job1 extends AtmosphereJob
{

	public void runAction()
	{
		final Callable<Void> callable = new Callable<Void>()
		{
			public Void call()
			{
				final String message = "Message from Job1";
				Job1.this.sendMessages("job1", message);
				return null;
			}
		};
		
	}
}
