package com.badlogic.gdx.setup;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.PagedIterable;
import org.kohsuke.github.RateLimitHandler;

import com.badlogic.gdx.setup.rest.NotFoundException;

public class GdxProject {

	public void generateProject(GdxProjectData projectData,
			ConcurrentHashMap<String, List<GdxTemplateFile>> cachedVersionFilesRepo, ZipOutputStream zipOutputStream)
			throws Exception {
		// 60 calls per hour is allowed... we have 10 here, but without subdirectories.
		// It is needed to cache,
		// or pull in the contents for released versions.

		List<GdxTemplateFile> files = cachedVersionFilesRepo.get(projectData.targetGdxVersion);

		if (files == null) {
			files = fetchFilesFromGithub(projectData);		
			cachedVersionFilesRepo.put(projectData.targetGdxVersion, files);
		}

		for (GdxTemplateFile file : files) {
			zipOutputStream.putNextEntry(new ZipEntry(file.name));
			zipOutputStream.write(file.content);
			zipOutputStream.closeEntry();
		}

	}

	private List<GdxTemplateFile> fetchFilesFromGithub(GdxProjectData projectData) throws IOException {
		List<GdxTemplateFile> files;
		GitHub githubClient = new GitHubBuilder().withRateLimitHandler(RateLimitHandler.FAIL).build();
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
			throw new NotFoundException("No release with name " + projectData.targetGdxVersion + " found");
		}

		files = new LinkedList<>();

		// ok, we have the release - list its files
		List<GHContent> dirContent = libgdxRepo.getDirectoryContent(
				"extensions/gdx-setup/res/com/badlogic/gdx/setup/resources", targetRelease.getTagName());
		for (GHContent content : dirContent) {
			if (content.getDownloadUrl() != null) {

				GdxTemplateFile templateFile = new GdxTemplateFile();

				templateFile.name = content.getName();

				try (BufferedInputStream in = new BufferedInputStream(
						new URL(content.getDownloadUrl()).openStream())) {
					ByteArrayOutputStream buffer = new ByteArrayOutputStream();
					int nRead;
					byte[] data = new byte[1024];
					while ((nRead = in.read(data, 0, data.length)) != -1) {
						buffer.write(data, 0, nRead);
					}

					buffer.flush();
					templateFile.content = buffer.toByteArray();
				} catch (IOException e) {
					// handle exception
				}

				files.add(templateFile);

			}
		}
		return files;
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
