/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 *
 */
package org.alienlabs.multirequest;

import java.util.Date;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.markup.html.WebPage;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceEventListener;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.cpr.Meteor;
import org.atmosphere.jersey.JerseyBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Suspend the response using the {@link Meteor} API.
 * 
 * @author Andrey Belyaev
 * @author Jeanfrancois Arcand
 */
public class ClockPage extends WebPage implements AtmosphereResourceEventListener
{

	static final Logger logger = LoggerFactory.getLogger(ClockPage.class);

	public ClockPage()
	{
		final HttpServletRequest req = this.getWebRequestCycle().getWebRequest()
				.getHttpServletRequest();

		// Grap a Meteor
		final Meteor meteor = Meteor.build(req);
		req.getRequestedSessionId();

		// Start scheduling update.
		final Callable<String> callable = new Callable<String>()
		{
			public String call()
			{
				final String message = new Date().toString();
				ClockPage.this.sendMessages("job1", message);
				ClockPage.logger.info(message);
				return message;
			}
		};
		meteor.schedule(callable, 1); // 1 second

		// Add us to the listener list.
		meteor.addListener(this);

		final String transport = req.getHeader("X-Atmosphere-Transport");

		// Suspend the connection. Could be long-polling, streaming or
		// websocket.
		meteor.suspend(-1, !((transport != null) && transport.equalsIgnoreCase("long-polling")));
	}

	protected void sendMessages(final String topic, final String message)
	{
		final Broadcaster broadcaster = this.getBroadcaster(topic);
		if (broadcaster != null)
		{
			broadcaster.broadcast(message);
		}
	}

	protected Broadcaster getBroadcaster(final String topic)
	{
		return BroadcasterFactory.getDefault().lookup(JerseyBroadcaster.class, topic);
	}

	public void onBroadcast(
			final AtmosphereResourceEvent<HttpServletRequest, HttpServletResponse> event)
	{
		event.getResource().getRequest().getRequestedSessionId();
		// if (!jsessionid.equals(((String)event.getMessage()).split("###")[0]))
		// {
		// If we are using long-polling, resume the connection as soon as we
		// get an event.
		final String transport = event.getResource().getRequest()
				.getHeader("X-Atmosphere-Transport");
		if ((transport != null) && transport.equalsIgnoreCase("long-polling"))
		{
			final Meteor meteor = Meteor.lookup(event.getResource().getRequest());
			meteor.removeListener(this);
			meteor.resume();
		}
		// }
	}

	public void onSuspend(
			final AtmosphereResourceEvent<HttpServletRequest, HttpServletResponse> event)
	{
		final String transport = event.getResource().getRequest()
				.getHeader("X-Atmosphere-Transport");
		final HttpServletRequest req = event.getResource().getRequest();
		ClockPage.logger.info("Suspending the %s response from ip {}:{}",
				new Object[] { transport == null ? "websocket" : transport, req.getRemoteAddr(),
						req.getRemotePort() });
	}

	public void onResume(
			final AtmosphereResourceEvent<HttpServletRequest, HttpServletResponse> event)
	{
		final String transport = event.getResource().getRequest()
				.getHeader("X-Atmosphere-Transport");
		final HttpServletRequest req = event.getResource().getRequest();
		ClockPage.logger.info("Resuming the {} response from ip {}:{}",
				new Object[] { transport == null ? "websocket" : transport, req.getRemoteAddr(),
						req.getRemotePort() });
	}

	public void onDisconnect(
			final AtmosphereResourceEvent<HttpServletRequest, HttpServletResponse> event)
	{
		final String transport = event.getResource().getRequest()
				.getHeader("X-Atmosphere-Transport");
		final HttpServletRequest req = event.getResource().getRequest();
		ClockPage.logger.info("{} connection dropped from ip {}:{}",
				new Object[] { transport == null ? "websocket" : transport, req.getRemoteAddr(),
						req.getRemotePort() });
	}

	public void onThrowable(
			final AtmosphereResourceEvent<HttpServletRequest, HttpServletResponse> event)
	{
		ClockPage.logger.info("onThrowable()", event.throwable());
	}

}
