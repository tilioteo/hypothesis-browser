package org.hypothesis.browser;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

/**
 * Class to create shell filled with Browser widget. 
 * TODO : disable all keys, enable logging, exception dialog
 * 
 * @author Štefan Bunčiak
 * @author Kamil Morong - Hypothesis
 *
 */
public class Browser {
	private static HashMap<String, String> params;
	
	public Browser(Shell shell) {
		
		final org.eclipse.swt.browser.Browser browser = new org.eclipse.swt.browser.Browser(shell, SWT.NONE);
		String url = String.format("%s?%s=%s", params.get(Constants.PARAM_APPLICATION_URL), Constants.PARAM_TOKEN, params.get(Constants.PARAM_TOKEN));
		browser.setUrl(url);
		
		shell.setLayout(new FillLayout());
		boolean maximized = Boolean.parseBoolean(params.get(Constants.PARAM_MAXIMIZED)); 
		shell.setFullScreen(maximized);
		//shell.setMaximized(maximized);
		shell.setText("HypothesisBrowser");
		
		// disable right click
		Menu menu = new Menu(browser);
		browser.setMenu(menu);

		browser.addLocationListener(new LocationAdapter() {
			
			@Override
			public void changing(LocationEvent arg0) {
				System.out.println(arg0.location);
				if (arg0.location.endsWith(params.get(Constants.PARAM_END_PARAMETER))) {
					System.out.printf("Value '%s' found - application is going to close", params.get(Constants.PARAM_END_PARAMETER));
					System.exit(0);
				}
			}
		});
	}
		
	private static void initParameters(String[] args) {
		//System.err.println("initializing parameters: " + String.valueOf(args.length));
		params = new HashMap<String, String>();
		for (String arg : args) {
			if (arg.startsWith("-D")) {
				String[] splitted = arg.substring(2).split("=");
				if (splitted.length > 1 && splitted[1].length() > 0) {
					params.put(splitted[0], splitted[1]);
					System.out.printf("Parameter found: %s = %s\n", splitted[0], splitted[1]);
				}
			}
		}
	}
	
	private static boolean checkParameters() {
		//System.err.println("checking parameters...");
		return params.containsKey(Constants.PARAM_APPLICATION_URL) &&
				params.containsKey(Constants.PARAM_TOKEN) &&
				params.containsKey(Constants.PARAM_END_PARAMETER);
	}
	
	/** 
	 * @param args
	 */
	public static void main(String[] args) {
		initParameters(args);
		
		if (!checkParameters()) {
			System.err.println("...failed");
			System.exit(-1);
		}

		//System.err.println("creating display");
		Display display = new Display();
		System.err.println("creating shell");
		final Shell shell = new Shell(display, SWT.NONE);
		
		//System.err.println("creating browser");
		new Browser(shell);
				
		//System.err.println("opening browser");
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
		//System.err.println("finishing");
	}
}