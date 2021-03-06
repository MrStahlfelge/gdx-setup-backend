package com.badlogic.gdx.setup.rest;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.badlogic.gdx.setup.GdxProjectData;
import com.badlogic.gdx.setup.ProjectGeneratorService;
import com.badlogic.gdx.setup.ProjectGeneratorService.CachedProjects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@RestController
@CrossOrigin
public class ProjectGeneratorController {
	private final ProjectGeneratorService service;

	public ProjectGeneratorController(ProjectGeneratorService service) {
		this.service = service;
	}

	@GetMapping("/download/{id}")
	public ResponseEntity<Resource> downloadZipFile(@PathVariable String id) {

		CachedProjects zipFile = service.getZipFile(id);

		if (zipFile == null)
			throw new NotFoundException("Project not found");

		ByteArrayResource bar = new ByteArrayResource(zipFile.zippedContent);

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + "libgdxproject" + ".zip" + "\"")
				.body(bar);

	}

	@GetMapping("/generate")
	public GeneratorResponse generateProject(@RequestParam(defaultValue = "1.9.12") String gdxVersion,
			@RequestParam String appName,
			@RequestParam String mainClass,
			@RequestParam(defaultValue = "false") boolean withHtml,
			@RequestParam(defaultValue = "false") boolean withIos,
			@RequestParam(defaultValue = "false") boolean withDesktop,
			@RequestParam(defaultValue = "false") boolean withAndroid
			// add everything needed here...
			) {

		GdxProjectData projectData = new GdxProjectData();
		projectData.targetGdxVersion = gdxVersion;
		projectData.withHtml = withHtml;
		projectData.withAndroid = withAndroid;
		projectData.withDesktop = withDesktop;
		projectData.withIos = withIos;
		projectData.appName = appName;
		projectData.mainClass = mainClass;
		
		GeneratorResponse response = new GeneratorResponse();

		try {
			String zipFileId = service.generateAndZipGdxProject(projectData);
			response.downloadUrl = zipFileId;
			response.warnings = projectData.warnings.toArray(new String[0]);
		} catch (Throwable t) {
			response.errorMessage = t.getMessage();
			t.printStackTrace();
		}

		return response;
	}
	
	@JsonInclude(Include.NON_NULL)
	public static class GeneratorResponse {
		public String downloadUrl;
		public String errorMessage;
		public String[] warnings;
	}
}
