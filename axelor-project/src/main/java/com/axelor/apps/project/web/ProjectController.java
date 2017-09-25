/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.project.web;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanning;
import com.axelor.apps.project.service.ProjectService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class ProjectController {

	@Inject
	protected ProjectService projectService;

	public void generateProjectFromPartner(ActionRequest request, ActionResponse response){
		Partner partner = Beans.get(PartnerRepository.class).find(Long.valueOf(request.getContext().get("_idPartner").toString()));
		User user = AuthUtils.getUser();
		Project project = projectService.generateProject(null, partner.getName()+" project", user, user.getActiveCompany(), partner);
		response.setValues(project);
	}
	
	public void setDurationDays(ActionRequest request, ActionResponse response){
		Project project = request.getContext().asType(Project.class);
		long diffInDays = ChronoUnit.DAYS.between(project.getFromDate(),project.getToDate());
		BigDecimal duration = new BigDecimal(diffInDays);
		response.setValue("$visibleDuration", duration);
		response.setValue("duration", duration);
	}
	
	public void createPlanning(ActionRequest request, ActionResponse response){
		
		Project project = request.getContext().asType(Project.class);
		
		List<ProjectPlanning> projectPlannings = projectService.createPlanning(project);
		
		response.setView(ActionView.define(I18n.get("Project Planning"))
				.model(ProjectPlanning.class.getName())
				.add("calendar", "project-planning-calendar")
				.add("grid", "project-planning-grid")
				.add("form", "project-planning-form")
				.domain("self.id in :_planningIds")
				.context("_planningIds", projectPlannings.stream().map(it->it.getId()).collect(Collectors.toList()))
				.map());
				
	}
	
}