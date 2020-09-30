package com.badlogic.gdx.setup;

import java.util.LinkedList;
import java.util.List;

public class GdxProjectData {
	public String targetGdxVersion;
	public String appName;
	public String mainClass;
	public boolean withAndroid;
	public boolean withIos;
	public boolean withHtml;
	public boolean withDesktop;

	public List<String> warnings = new LinkedList<>();
}
