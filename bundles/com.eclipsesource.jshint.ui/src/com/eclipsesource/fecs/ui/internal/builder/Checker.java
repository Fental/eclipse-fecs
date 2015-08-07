/*******************************************************************************
 * Copyright (c) 2015 EclipseSource.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    huangfengtao - initial implementation and API
 ******************************************************************************/
package com.eclipsesource.fecs.ui.internal.builder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.fecs.Problem;
import com.eclipsesource.fecs.ProblemHandler;
import com.eclipsesource.fecs.Text;
import com.eclipsesource.fecs.internal.ProblemImpl;

public class Checker {
	public String check(IFile resource, Text code, ProblemHandler handler) throws InterruptedException {
		try {
			IPath path = resource.getRawLocation();
			String text = "";
			text += path;
			String[] command = new String[] { "/bin/zsh", "-c",
					"/Users/huangfengtao/.nvm/versions/node/v0.12.7/bin/fecs " + text
							+ " --reporter baidu --rule true --sort true --silent true --format json"
					// + " --silent true --format xml"
			};
			// String[] command = new String[]{"/bin/zsh", "-c", "which npm"};
			Process process = Runtime.getRuntime().exec(command);
			process.waitFor();
			// 为什么fecs输出的东西显示不出来
			BufferedReader br = new BufferedReader(
					new InputStreamReader(process.getInputStream(), Charset.forName("utf-8")));
			String result = "";
			String line = null;
			while ((line = br.readLine()) != null) {
				result += line;
				// System.out.println(line);
			}

			handleProblems(handler, code, result);
			return result;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void handleProblems(ProblemHandler handler, Text text, String result) {
		// NativeArray errors = (NativeArray)jshint.get( "errors", jshint );
		// long length = errors.getLength();
		//
		// for( int i = 0; i < length; i++ ) {
		// Object object = errors.get( i, errors );
		// ScriptableObject error = (ScriptableObject)object;
		// if( error != null ) {
		// Problem problem = createProblem( error, text );
		// handler.handleProblem( problem );
		// }
		// }
		JsonArray json = JsonArray.readFrom(result);
		JsonArray errors = null;
		if (!json.isEmpty()) {
			errors = (JsonArray) (((JsonObject) json.get(0)).get("errors"));
			int length = errors.size();
			System.out.println(length);
			for (int i = 0; i < length; i++) {
				JsonObject error = (JsonObject) errors.get(i);
				// System.out.println(error);
				if (error != null) {
					Problem problem = createProblem(error, text);
					// System.out.println("fuck");
					handler.handleProblem(problem);
				}
			}
			System.out.println(errors);
		}
	}

	ProblemImpl createProblem(JsonObject error, Text text) {
		String reason = error.get("message").asString();
		int line = error.get("line").asInt();
		int severity = error.get("severity").asInt();
		int character = error.get("column").asInt();
		String code = error.get("rule").asString();

		if (line <= 0 || line > text.getLineCount()) {
			line = -1;
			character = -1;
		} else if (character > 0) {
			character = visualToCharIndex(text, line, character);
		}

		String message = reason;
		return new ProblemImpl(line, character, message, code, severity);
	}

	/*
	 * JSHint reports "visual" character positions instead of a character index,
	 * i.e. the first character is 1 and every tab character is multiplied by
	 * the indent with.
	 *
	 * Example: "a\tb\tc"
	 *
	 * index: | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10| char: | a | » | b |
	 * » | c | visual: | a | » | b | » | c |
	 */
	int visualToCharIndex(Text text, int line, int character) {
		String string = text.getContent();
		int offset = text.getLineOffset(line - 1);
		int charIndex = 0;
		int visualIndex = 1;
		int maxCharIndex = string.length() - offset - 1;
		while (visualIndex != character && charIndex < maxCharIndex) {
			boolean isTab = string.charAt(offset + charIndex) == '\t';
			visualIndex += isTab ? 4 : 1;
			charIndex++;
		}
		return charIndex;
	}

	public String format(IFile resource, IProgressMonitor monitor) throws InterruptedException {
		try {
			IPath path = resource.getRawLocation();
			String text = "";
			text += path;
			String[] command = new String[] { "/bin/zsh", "-c",
					"/Users/huangfengtao/.nvm/versions/node/v0.12.7/bin/fecs check " + text
					// + " --replace true"
			};
			// String[] command = new String[]{"/bin/zsh", "-c", "which npm"};
			Process process = Runtime.getRuntime().exec(command);
			process.waitFor();
//			Process or = process.getInputStream();
			try {
				resource.setContents(process.getInputStream(), true, false, monitor);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// 为什么fecs输出的东西显示不出来
//			BufferedReader br = new BufferedReader(
//					new InputStreamReader(process.getInputStream(), Charset.forName("utf-8")));
//			String result = "";
//			String line = null;
//			while ((line = br.readLine()) != null) {
//				result += line;
//				// System.out.println(line);
//			}
//
//			handleProblems(handler, code, result);
//			return result;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
