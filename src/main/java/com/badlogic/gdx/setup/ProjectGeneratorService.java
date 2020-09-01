package com.badlogic.gdx.setup;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.badlogic.gdx.setup.GdxProject.GdxProjectData;

@Service
public class ProjectGeneratorService {
	private ConcurrentHashMap<String, CachedProjects> generatedFiles = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, List<GdxTemplateFile>> cachedVersionFilesRepo = new ConcurrentHashMap<>();

	public String generateAndZipGdxProject(GdxProjectData projectData) throws Exception {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream zipOutputStream = new ZipOutputStream(baos);

		new GdxProject().generateProject(projectData, cachedVersionFilesRepo, zipOutputStream);

		// this is generated completely dynamical
		zipOutputStream.putNextEntry(new ZipEntry("build.gradle"));
		zipOutputStream.write("FROM MRSTAHLFELGE WITH LOVE".getBytes());
		zipOutputStream.closeEntry();

		zipOutputStream.close();

		clearCache();

		String uuid = UUID.randomUUID().toString();
		generatedFiles.put(uuid, new CachedProjects(baos.toByteArray()));

		return uuid;

	}

	@Scheduled(fixedRate = 1000 * 60 * 5, initialDelay = 10000)
	public void clearCache() {
		List<String> uuids = new ArrayList<>(generatedFiles.keySet());

		long timeNow = System.currentTimeMillis();
		long oldestEntryTime = timeNow;
		String oldestEntryUuid = null;

		// remove everything older than 10 minutes
		for (String uuid : uuids) {
			CachedProjects project = generatedFiles.get(uuid);

			if (project != null) {
				if (timeNow - project.timestamp > 1000 * 60 * 10)
					generatedFiles.remove(uuid);
				else if (project.timestamp < oldestEntryTime && timeNow - project.timestamp > 1000 * 60 * 5) {
					oldestEntryUuid = uuid;
					oldestEntryTime = project.timestamp;
				}
			}
		}

		// never more than 5 cached zip files at a time
		if (generatedFiles.size() > 5 && oldestEntryUuid != null) {
			generatedFiles.remove(oldestEntryUuid);
		}
	}

	public CachedProjects getZipFile(String id) {
		return generatedFiles.get(id);
	}

	public static class CachedProjects {
		public final byte[] zippedContent;
		public final long timestamp;

		public CachedProjects(byte[] zipFile) {
			this.zippedContent = zipFile;
			timestamp = System.currentTimeMillis();
		}
	}

}
