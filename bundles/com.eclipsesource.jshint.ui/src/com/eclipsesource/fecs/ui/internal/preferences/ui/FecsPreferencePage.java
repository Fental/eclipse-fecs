/*******************************************************************************
 * Copyright (c) 2012, 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ralf Sternberg - initial implementation and API
 ******************************************************************************/
package com.eclipsesource.fecs.ui.internal.preferences.ui;

import static com.eclipsesource.fecs.ui.internal.util.LayoutUtil.gridData;
import static com.eclipsesource.fecs.ui.internal.util.LayoutUtil.gridLayout;

import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
//import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

//import com.eclipsesource.fecs.JSHint;
import com.eclipsesource.fecs.ui.internal.Activator;
import com.eclipsesource.fecs.ui.internal.builder.BuilderUtil;
import com.eclipsesource.fecs.ui.internal.builder.FecsBuilder;
import com.eclipsesource.fecs.ui.internal.preferences.FecsPreferences;

// 首选项页

public class FecsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private final FecsPreferences preferences;
	private Button defaultLibRadio;
	private Button customLibRadio;
	private Text customLibPathText;
	private Button customLibPathButton;
	private Button enableErrorsCheckbox;

	public FecsPreferencePage() {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("General settings for FECS");

		preferences = new FecsPreferences();
	}

	public void init(IWorkbench workbench) {
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return null;
	}

	// 使用PreferencePage可以使用不同类型的字段编辑器，但必须自己做更多地工作，
	// 载入，验证和保存值
	// 创建Preference Page内容
	@Override
	protected Control createContents(Composite parent) {
		// 组合部件
		Composite composite = new Composite(parent, SWT.NONE);
		// 布局
		gridLayout(composite).columns(3).spacing(3).marginTop(10);
		createCustomFecsArea(composite);
		createEnableErrorMarkersArea(composite);
		updateControlsFromPrefs();
		updateControlsEnabled();
		return composite;
	}

	// 应用按钮
	@Override
	public boolean performOk() {
		try {
			System.out.println("是否改变首选项");
			System.out.println(preferences.hasChanged());

			if (preferences.hasChanged()) {
				preferences.save();
				triggerRebuild();
			}
		} catch (CoreException exception) {
			Activator.logError("Failed to save preferences", exception);
			return false;
		}
		return true;
	}

	// 恢复默认
	@Override
	protected void performDefaults() {
		preferences.resetToDefaults();
		updateControlsFromPrefs();
		updateControlsEnabled();
		super.performDefaults();
	}

	// 创建选择JSHint文件的单选框
	private void createCustomFecsArea(Composite parent) {
		// 默认选中的radio
		defaultLibRadio = new Button(parent, SWT.RADIO);
		// 获取JSHint版本
		// String version = JSHint.getDefaultLibraryVersion();
		// #1 使用默认的node bin路径
		defaultLibRadio.setText("Use default directory of node interpreter");

		// 布局
		gridData(defaultLibRadio).fillHorizontal().span(3, 1);

		// 选中即可选择node bin路径
		customLibRadio = new Button(parent, SWT.RADIO);
		customLibRadio.setText("Provide the directory of node interpreter");

		// 布局
		gridData(customLibRadio).fillHorizontal().span(3, 1);

		// 为选择node bin的radio按钮添加监听事件
		customLibRadio.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				preferences.setUseCustomLib(customLibRadio.getSelection());
				validate();
//				validatePrefs();
				// 让选择目录的按钮使能
				updateControlsEnabled();
			}
		});

		// 添加路径
		customLibPathText = new Text(parent, SWT.BORDER);
		gridData(customLibPathText).fillHorizontal().span(2, 1).indent(25, 0);
		customLibPathText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				preferences.setCustomNodeDir(customLibPathText.getText());
				// preferences.setCustomLibPath(customLibPathText.getText());
				validate();
//				validatePrefs();
			}
		});

		// 选择文件的按钮
		customLibPathButton = new Button(parent, SWT.PUSH);
		customLibPathButton.setText("Select");
		customLibPathButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				// selectFile();
				selectDir();
			}
		});
		Text customLibPathLabelText = new Text(parent, SWT.READ_ONLY | SWT.WRAP);
		customLibPathLabelText.setText("This dir must have FECS.");
		customLibPathLabelText.setBackground(parent.getBackground());
		gridData(customLibPathLabelText).fillHorizontal().span(3, 1).indent(25, 1);
	}

	// checkbox，勾选后则允许使用error marker
	private void createEnableErrorMarkersArea(Composite parent) {
		enableErrorsCheckbox = new Button(parent, SWT.CHECK);
		enableErrorsCheckbox.setText("Enable FECS errors");
		enableErrorsCheckbox.setToolTipText("If unchecked, errors will be shown as warnings");
		gridData(enableErrorsCheckbox).fillHorizontal().span(3, 1);
		enableErrorsCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				preferences.setEnableErrorMarkers(enableErrorsCheckbox.getSelection());
				validate();
//				validatePrefs();
			}
		});
	}

	// private void selectFile() {
	// FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
	// fileDialog.setText("Select JSHint library file");
	// File file = new File(preferences.getCustomLibPath());
	// fileDialog.setFileName(file.getName());
	// fileDialog.setFilterPath(file.getParent());
	// fileDialog.setFilterNames(new String[] { "JavaScript files" });
	// fileDialog.setFilterExtensions(new String[] { "*.js", "" });
	// String selectedPath = fileDialog.open();
	// if (selectedPath != null) {
	// customLibPathText.setText(selectedPath);
	// }
	// }

	private void selectDir() {
		// 新建文件夹（目录）对话框
		DirectoryDialog folder = new DirectoryDialog(getShell(), SWT.OPEN);
		// 设置文件对话框的标题
		folder.setText("Select FECS bin directory");
		// 设置初始路径
		folder.setFilterPath("SystemDrive");
		// 设置对话框提示文本信息
		// folder.setMessage("请选择相应的文件夹");
		// 打开文件对话框，返回选中文件夹目录
		String selectedDir = folder.open();
		if (selectedDir != null) {
			customLibPathText.setText(selectedDir);
		}
	}

	private void validate() {
		setErrorMessage(null);
		setValid(true);
		final Display display = getShell().getDisplay();
		Job validator = new Job("JSHint preferences validation") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					monitor.beginTask("checking preferences", 1);
					validatePrefs();
					display.asyncExec(new Runnable() {
						public void run() {
							setValid(true);
						}
					});
				} catch (final IllegalArgumentException exception) {
					display.asyncExec(new Runnable() {
						public void run() {
							setErrorMessage(exception.getMessage());
						}
					});
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		validator.schedule();
	}

	// private void validatePrefs() {
	// if (preferences.getUseCustomLib()) {
	// String path = preferences.getCustomLibPath();
	// validateFile(new File(path));
	// }
	// }
	//

	// private static void validateFile(File file) throws
	// IllegalArgumentException {
	// if (!file.isFile()) {
	// throw new IllegalArgumentException("File does not exist");
	// }
	// if (!file.canRead()) {
	// throw new IllegalArgumentException("File is not readable");
	// }
	// try {
	// FileInputStream inputStream = new FileInputStream(file);
	// try {
	// JSHint jsHint = new JSHint();
	// jsHint.load(inputStream);
	// } finally {
	// inputStream.close();
	// }
	// } catch (Exception exception) {
	// throw new IllegalArgumentException("File is not a valid JSHint library",
	// exception);
	// }
	// }

	// #1

	private void validatePrefs() {
		if (preferences.getUseCustomLib()) {
			String dir = preferences.getCustomNodeDir();
			validateDir(dir);
		} else {
			validateDir("");
		}
	}

	private static void validateDir(String dir) {
		try {
			String command;
			if (dir.equalsIgnoreCase("") || dir.equalsIgnoreCase("/")) {
				command = dir + "node -v";
			} else {
				command = dir + "/node -v";
			}

			Process process = Runtime.getRuntime().exec(command);
			try {
				process.waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			BufferedReader br = new BufferedReader(
					new InputStreamReader(process.getInputStream(), Charset.forName("utf-8")));
			String result = "";
			String line = null;
			while ((line = br.readLine()) != null) {
				result += line;
				System.out.println(line);
			}
			if (result == "") {
				throw new IllegalArgumentException("Directory is not a valid interpreter Directort");
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("Directory is not a valid interpreter Directort");
		}
	}

	private void updateControlsFromPrefs() {
		customLibRadio.setSelection(preferences.getUseCustomLib());
		defaultLibRadio.setSelection(!customLibRadio.getSelection());
//		System.out.println("fuck");
//		System.out.println(preferences.getCustomNodeDir());
		customLibPathText.setText(preferences.getCustomNodeDir());
		enableErrorsCheckbox.setSelection(preferences.getEnableErrorMarkers());
	}

	private void updateControlsEnabled() {
		boolean enabled = customLibRadio.getSelection();
		customLibPathText.setEnabled(enabled);
		customLibPathButton.setEnabled(enabled);
	}

	private void triggerRebuild() throws CoreException {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (IProject project : projects) {
			if (project.isAccessible()) {
				BuilderUtil.triggerClean(project, FecsBuilder.ID);
			}
		}
	}

}
