package com.badlogic.gdx.setup;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;

public class GdxProject {

	public void generateProject(GdxProjectData projectData, ZipOutputStream zipOutputStream) throws Exception {
		// 60 calls per hour is allowed... we have 10 here, but without subdirectories.
		// It is needed to cache,
		// or pull in the contents for released versions.
		GitHub githubClient = GitHub.connectAnonymously();
		GHRepository libgdxRepo = githubClient.getRepository("libgdx/libgdx");

		PagedIterable<GHRelease> releaseList = libgdxRepo.listReleases();

		GHRelease targetRelease = null;

		for (GHRelease release : releaseList) {
			if (release.getName().equals(projectData.targetGdxVersion)) {
				targetRelease = release;
				break;
			}
		}

		if (targetRelease == null) {
			throw new Exception("No release with name " + projectData.targetGdxVersion + " found");
		}

		// ok, we have the release - list its files
		List<GHContent> dirContent = libgdxRepo.getDirectoryContent(
				"extensions/gdx-setup/res/com/badlogic/gdx/setup/resources", targetRelease.getTagName());
		for (GHContent content : dirContent) {
			if (content.getDownloadUrl() != null) {
				zipOutputStream.putNextEntry(new ZipEntry(content.getName()));
				// zipOutputStream.write();

				try (BufferedInputStream in = new BufferedInputStream(new URL(content.getDownloadUrl()).openStream())) {
					byte dataBuffer[] = new byte[1024];
					int bytesRead;
					while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
						zipOutputStream.write(dataBuffer, 0, bytesRead);
					}
				} catch (IOException e) {
					// handle exception
				}
				zipOutputStream.closeEntry();
			}
		}
	}

	public static class GdxProjectData {
		public String targetGdxVersion;
		public boolean withAndroid;
		public boolean withIos;
		public boolean withHtml;
		public boolean withDesktop;

		public List<String> warnings = new LinkedList<>();
	}
}
