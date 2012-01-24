package org.alienlabs.multirequest;

import java.util.Date;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.resources.JavaScriptReference;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomePage extends WebPage
{
	private static final long serialVersionUID = 1L;
	static final Logger logger = LoggerFactory.getLogger(HomePage.class);
	private AbstractReadOnlyModel<String> time;

	public HomePage()
	{
		this.add(new JavaScriptReference("jquery-1.7.min.js", HomePage.class,
				"js/jquery-1.7.min.js"));
		this.add(new JavaScriptReference("jquery.atmosphere.js", HomePage.class,
				"js/jquery.atmosphere.js"));
		this.add(new JavaScriptReference("main.js", HomePage.class, "js/main.js"));

		this.add(new Label("version", this.getApplication().getFrameworkSettings().getVersion()));
		this.add(new BookmarkablePageLink<ClockPage>("cometStart", ClockPage.class));
		this.time = new AbstractReadOnlyModel<String>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject()
			{
				return new Date().toString();
			}
		};
		this.add(new Label("clock", this.time));
	}

}
