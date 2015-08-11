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
package com.eclipsesource.fecs.ui.internal.builder;

import java.util.Map;

import org.eclipse.core.resources.IFile;
// 参考链接http://help.eclipse.org/juno/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Freference%2Fextension-points%2Forg_eclipse_core_resources_builders.html
// 接口
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
// import org.eclipse.core.resources.IResourceDeltaVisitor;
// all incremental project builders 的抽象基类，想要写builder就得继承它
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ProjectScope;
// core error
import org.eclipse.core.runtime.CoreException;
//import org.eclipse.core.runtime.IPath;
// 接口
// 功能：这个接口被对象实现，用来监测progress of an activity
import org.eclipse.core.runtime.IProgressMonitor;
import com.eclipsesource.fecs.ui.internal.Activator;
import com.eclipsesource.fecs.ui.internal.preferences.OptionsPreferences;

//import com.eclipsesource.jshint.ui.internal.builder.Checker;
import static com.eclipsesource.fecs.ui.internal.util.IOUtil.writeFileUtf8;

// Builder类继承抽象基类
public class FecsBuilder extends IncrementalProjectBuilder {

	public static final String ID = Activator.PLUGIN_ID + ".builder";
	public static final String ID_OLD = "com.eclipsesource.fecs.builder";

	// private String result = null;

	@Override
	// 重载
	// 功能：重新构建项目时，Eclipse就会调用这个方法
	// builder 有两种触发方式：1、nature配置，2、用特定的代码触发，这个插件的触发条件是在property page触发

	// 如果build kind 为INCREMENTAL_BUILD或者AUTO_BUILD或者FULL_BUILD，
	// AUTO_BUILD：如果平台监测到项目已经改变，那么构建器应该执行增量构建
	// FULL_BUILD：如果用户（例如通过，菜单）已请求完整地重新构建，那么就实现完整地构建。(给项目重新命名的时候触发了FULL_BUILD)
	// INCREMRNTAL_BUILD：如果用户请求增量构建，那么就实现构建
	// 参考资料:http://www.ibm.com/developerworks/cn/xml/x-wxxm/part16/

	// getDelta 方法能够被使用，用于获得这次调用与上次调用的资源增量
	// 完成build，这个builder将会返回projects列表 for which it requires a resource delta
	// the next time it is run

	// 供构建器更新用户的进度监视器。该监视器控制一个进度条和一个停止按钮。由于构建过程可能会很慢，因此构建器应该测试用户是否已取消了该操作。
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		checkFecsrc(getProject());
		if (kind == IncrementalProjectBuilder.FULL_BUILD) {
			fullBuild(monitor);
		} else {
			// getProject()获得绑定了当前builder的project
			// 获取增量
			// 平台通过IResourceDelta接口报告这些更改，构建器通过调用getDelta()方法来检索IResourceDelta的实例
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				// 增量构建
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	private void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
//		// 进行编辑器内容获取测试
//		Display.getDefault().asyncExec(new Runnable() {
//		    public void run() {
//				//取得工作台
//				IWorkbench workbench = PlatformUI.getWorkbench();
//				//取得工作台窗口
//				IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
//				//取得工作台页面
//				IWorkbenchPage page = window.getActivePage();
//				//取得当前处于活动状态的编辑器窗口
//				IEditorPart part = page.getActiveEditor();		        
//
//				System.out.println(part.getEditorInput());
//		    }
//		});
//		
		
		delta.accept(new FecsBuilderVisitor(getProject(), monitor));
		
		// System.out.println("incremental build on " + delta);
		// IResourceDelta delta
		// 访问者模式
		// 该数据结构接受visitor对象，并对数据结构中的每一项调用其visit()方法，IResourceDelta对每个文件更改调用visit()
	}

	private void fullBuild(IProgressMonitor monitor) throws CoreException {
		System.out.println("full build");
		IProject project = getProject();
		// IProject 的accept方法
		getProject().accept(new FecsBuilderVisitor(project, monitor));
	}

	private void checkFecsrc(IProject project) {
		// 获取首选项先
		OptionsPreferences prefs = new OptionsPreferences(
			new ProjectScope(project).getNode(Activator.PLUGIN_ID)
		);
		
		// 配置文件
		IFile config = project.getFile(".fecsrc");
		// TODO 判断是否要写.fecsrc
		if (config.exists() && prefs.getProjectSpecific() == true) {
			return;
		}
		else {
			// 使用首选项的配置，写至.fecsrc中
			try {
				writeFileUtf8(config, prefs.getConfig());
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		// TODO
		// 清除marker
		new MarkerAdapter(getProject()).removeMarkers();
		// System.out.println("remove");
	}

	static class CoreExceptionWrapper extends RuntimeException {

		private static final long serialVersionUID = 2267576736168605043L;

		public CoreExceptionWrapper(CoreException wrapped) {
			super(wrapped);
		}

	}
}