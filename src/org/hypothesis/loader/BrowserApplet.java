package org.hypothesis.loader;

import java.applet.Applet;

import org.hypothesis.browser.Browser;
import org.hypothesis.browser.Constants;

@SuppressWarnings("serial")
public class BrowserApplet extends Applet {
	
	private String url;
	private String end;
	
	public void init() {
		
		url = getParameter(Constants.PARAM_APPLICATION_URL);
		end = getParameter(Constants.PARAM_END_PARAMETER);
		
		//startBrowser(getParameter(Constants.PARAM_TOKEN));
	}
	
	public void startBrowser(String token) {
		Browser.main(new String[] {"-D"+Constants.PARAM_APPLICATION_URL+"="+url, "-D"+Constants.PARAM_TOKEN+"="+token, "-D"+Constants.PARAM_END_PARAMETER+"="+end});
	}

}
