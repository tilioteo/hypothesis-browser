/**
 * 
 */
package org.hypothesis.loader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.jdt.internal.jarinjarloader.RsrcURLStreamHandlerFactory;
import org.hypothesis.browser.Constants;

/**
 * @author Kamil Morong - Hypothesis
 *
 */
public class BrowserLoader {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Throwable {
		HashMap<String, String> arguments = new HashMap<String, String>();

		for (String parameter : Constants.PARAMETERS) {
			String value = System.getProperty(parameter);
			if (value != null)
				arguments.put(parameter, value);
		}
		
		//System.err.println("Arguments: " + String.valueOf(args.length));
		// override by arguments specified in command line
		for (String arg : args) {
			if (arg.startsWith("-D")) {
				String[] pair = arg.substring(2).split("=");
				if (pair.length == 2) {
					if (pair[1].length() > 0)
						arguments.put(pair[0], pair[1]);
					else
						arguments.remove(pair[0]);
				}
			}
		}
				
				
		ClassLoader cl = getSWTClassloader();
		Thread.currentThread().setContextClassLoader(cl);    
		try {
			try {
				//System.err.println("Launching Browser ...");
				Class<?> c = Class.forName("org.hypothesis.browser.Browser", true, cl);
				//System.err.println("found class " + c.getName());
				Method main = c.getMethod("main", new Class[]{args.getClass()});
				
				String[] newArgs = new String[arguments.size()];
				Iterator<String> iterator = arguments.keySet().iterator();
				int i = 0;
				while (iterator.hasNext()) {
					String name = iterator.next();
					newArgs[i++] = String.format("-D%s=%s", name, arguments.get(name));
				}
				//System.err.println("New arguments: " + String.valueOf(newArgs.length));
				
				main.invoke((Object)null, new Object[] {newArgs});
				
			} catch (InvocationTargetException ex) {
				if (ex.getCause() instanceof UnsatisfiedLinkError) {
					System.err.println("Launch failed: (UnsatisfiedLinkError)");
					String arch = getArch();
					if ("32".equals(arch)) {
						System.err.println("Try adding '-d64' to your command line arguments");
					} else if ("64".equals(arch)) {
						System.err.println("Try adding '-d32' to your command line arguments");
					}
				} else {
					throw ex;
				}
			}
		} catch (ClassNotFoundException ex) {
			System.err.println("Launch failed: Failed to find main class - org.hypothesis.browser.Browser");
		} catch (NoSuchMethodException ex) {
			System.err.println("Launch failed: Failed to find main method");
		} catch (InvocationTargetException ex) {
			Throwable th = ex.getCause();
			if ((th.getMessage() != null) && th.getMessage().toLowerCase().contains("invalid thread access")) {
				System.err.println("Launch failed: (SWTException: Invalid thread access)");
				System.err.println("Try adding '-XstartOnFirstThread' to your command line arguments");
			} else {
				throw th;
			}
		}
	}

	private static ClassLoader getSWTClassloader() {
		ClassLoader parent = BrowserLoader.class.getClassLoader();    
		URL.setURLStreamHandlerFactory(new RsrcURLStreamHandlerFactory(parent));
		String swtFileName = getSwtJarName();      
		try {
			URL intraceFileUrl = new URL("rsrc:hypothesis-browser-wrapper.jar");
			URL swtFileUrl = new URL("rsrc:" + swtFileName);
			System.err.println("Using SWT Jar: " + swtFileName);
			ClassLoader cl = new URLClassLoader(new URL[] {intraceFileUrl, swtFileUrl}, parent);

			try {
				// Check we can now load the SWT class
				Class.forName("org.eclipse.swt.widgets.Layout", true, cl);
			} catch (ClassNotFoundException exx) {
				System.err.println("Launch failed: Failed to load SWT class from jar: " + swtFileName);
				throw new RuntimeException(exx);
			}

			return cl;
		} catch (MalformedURLException exx) {
			throw new RuntimeException(exx);
		}
	}

	private static String getSwtJarName() {
		// Detect OS
		String osName = System.getProperty("os.name").toLowerCase();    
		String swtFileNameOsPart = osName.contains("win") ? "win32-windows" : osName.contains("mac") ? "macosx" : osName.contains("linux") || osName.contains("nix") ? "gtk-linux" : "";
		if ("".equals(swtFileNameOsPart)) {
			throw new RuntimeException("Launch failed: Unknown OS name: " + osName);
		}

		// Detect 32bit vs 64 bit
		String swtFileNameArchPart = getArch();

		String swtFileName = "swt-" + swtFileNameOsPart + "-" + swtFileNameArchPart + ".jar";
		return swtFileName;
	}

	private static String getArch() {
		// Detect 32bit vs 64 bit
		String jvmArch = System.getProperty("os.arch").toLowerCase();
		String arch = (jvmArch.contains("64") ? "x86_64" : "x86");
		return arch;
	}
}
