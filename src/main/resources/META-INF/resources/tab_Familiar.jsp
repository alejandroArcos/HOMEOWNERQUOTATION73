<%@ include file="./init.jsp"%>

<c:set var="oculto" value="${ perfilMayorEjecutivo ? '' : 'd-none' }"></c:set>

<div class="card">
	<div class="card-body">
		<div class="accordion md-accordion" id="accordionAsegurado-${count}" role="tablist" aria-multiselectable="true">
			<div class="card">
				<div class="card-header btn-blue modificado" role="tab" id="headingAseguradoAdicional-${count}">
					<a class="collapsed" data-toggle="collapse" data-parent="#accordionAsegurado-${count}" href="#collapseAseguradoAdicional-${count}" aria-expanded="false" aria-controls="collapseAseguradoAdicional-${count}">
						<h5 class="mb-0">
							<liferay-ui:message key="CotizadorPaso2Portlet.titDatAsegurado" />
							<i class="fas fa-angle-down rotate-icon"></i>
						</h5>
					</a>
				</div>
			</div>
			<div id="collapseAseguradoAdicional-${count}" class="collapse in" role="tabpanel" aria-labelledby="headingAseguradoAdicional-${count}" data-parent="#accordionAsegurado-${count}">
				<div class="card-body">
					<div class="row">
						<div class="col-md-5">
							<liferay-ui:message key="CotizadorPaso2Portlet.lblPreguntaAsegurado" />
						</div>
						<div class="col-md-7">
					    	<input class="form-check-input pull-right checkAsegurado" type="checkbox" id="checkAsegurado-${count}" value="" />
					    	<label for="checkAsegurado-${count}">
					        	<span>Sí</span>
					    	</label>
						</div>
					</div>
					<div class="dynamicAsegurado">
					</div>
				</div>
			</div>
			<div class="card">
				<div class="card-header btn-blue modificado" role="tab" id="headingBeneficiarioPreferente-${count}">
					<a class="collapsed" data-toggle="collapse" data-parent="#accordionAsegurado-${count}" href="#collapseBeneficiarioPreferente-${count}" aria-expanded="false" aria-controls="collapseBeneficiarioPreferente-${count}">
						<h5 class="mb-0">
							<liferay-ui:message key="CotizadorPaso2Portlet.titDatBeneficiario" />
							<i class="fas fa-angle-down rotate-icon"></i>
						</h5>
					</a>
				</div>
			</div>
			<div id="collapseBeneficiarioPreferente-${count}" class="collapse in" role="tabpanel" aria-labelledby="headingBeneficiarioPreferente-${count}" data-parent="#accordionAsegurado-${count}">
				<div class="card-body">
					<div class="row">
						<div class="col-md-5">
							<liferay-ui:message key="CotizadorPaso2Portlet.lblPreguntaBeneficiario" />
						</div>
						<div class="col-md-7">
					    	<input class="form-check-input pull-right checkBeneficiario" type="checkbox" id="checkBeneficiario-${count}" value="" />
					    	<label for="checkBeneficiario-${count}">
					        	<span>Sí</span>
					    	</label>
						</div>
					</div>
					<div class="dynamicBeneficiario">
					</div>
				</div>
			</div>
		</div>
	</div>
</div>
<div class="card">
	<div class="card-body">
		<h6 id="tit_tab">
			<liferay-ui:message key="CotizadorPaso2Portlet.titDatsRiesgP2" />
		</h6>
		<div class="row">
			<div class="col-md-1">
				<div class="md-form">
					<input type="text" name="dr_cp-${count}" id="dr_cp-${count}" idCp="${ ubicacion.ubicaciones[count].cpData.idCp}" maxlength="5" class="form-control cpValido infReq" value="${ ubicacion.ubicaciones[count].cpData.cp}">
					<label for="dr_cp-${count}">
						<liferay-ui:message key="CotizadorPaso2Portlet.lblCodPosP2" />
					</label>
				</div>
			</div>
			<div class="col-md-3">
				<div class="md-form">
					<input type="text" name="dr_calle-${count}" id="dr_calle-${count}" class="form-control infReq" value="${ ubicacion.ubicaciones[count].calle}">
					<label for="dr_calle-${count}">
						<liferay-ui:message key="CotizadorPaso2Portlet.lblCalleP2" />
					</label>
				</div>
			</div>
			<div class="col-md-2">
				<div class="md-form form-group">
					<input type="text" name="dr_numero-${count}" id="dr_numero-${count}" class="form-control infReq" value="${ ubicacion.ubicaciones[count].numero}">
					<label for="dr_numero-${count}">
						<liferay-ui:message key="CotizadorPaso2Portlet.lblNumeroP2" />
					</label>
				</div>
			</div>
			<div class="col-md-2">
				<div class="md-form form-group">
					<input type="text" name="dr_numeroInt-${count}" id="dr_numeroInt-${count}" class="form-control" value="${ ubicacion.ubicaciones[count].numeroInterior}">
					<label for="dr_numeroInt-${count}">
						<liferay-ui:message key="CotizadorPaso2Portlet.lblNumeroIntP2" />
					</label>
				</div>
			</div>
			<div class="col-md-2">
				<div class="md-form form-group">
					<select name="dr_colonia-${count}" id="dr_colonia-${count}" class="mdb-select form-control-sel infReq">
						<option value="-1" selected><liferay-ui:message key="CotizadorPaso2Portlet.selectOpDefoult" /></option>
						<c:forEach items="${colonias[count]}" var="option">
							<c:set var="seleccionado" value="${ option.idCp == ubicacion.ubicaciones[count].cpData.idCp ? 'selected' : '' }"></c:set>
							<option ${ seleccionado } value="${option.idCp}">${option.colonia}</option>
						</c:forEach>
					</select>
					<label for="dr_colonia-${count}">
						<liferay-ui:message key="CotizadorPaso2Portlet.lblColoniaP2" />
					</label>
				</div>
			</div>
			<div class="col-md-2">
				<div class="md-form form-group">
					<input type="text" name="dr_municipio-${count}" id="dr_municipio-${count}" class="form-control infReq" value="${ ubicacion.ubicaciones[count].cpData.delegacion}">
					<label for="dr_municipio-${count}">
						<liferay-ui:message key="CotizadorPaso2Portlet.lblDelMuniP2" />
					</label>
				</div>
			</div>
		</div>
		<div class="row">
			<div class="col-md-3">
				<div class="md-form form-group">
					<input type="text" name="dr_estado-${count}" id="dr_estado-${count}" class="form-control infReq" value="${ ubicacion.ubicaciones[count].cpData.estado}">
					<label for="dr_estado-${count}">
						<liferay-ui:message key="CotizadorPaso2Portlet.lblEdoP2" />
					</label>
				</div>
			</div>
			<div class="col-sm-3">
				<div class="md-form form-group">
					<select name="tip_inm-${count}" id="tip_inm-${count}" class="mdb-select tip_inm infReq">
						<option value="-1" selected><liferay-ui:message key="CotizadorPaso2Portlet.selectOpDefoult" /></option>
						<c:forEach items="${listaInmuebles}" var="opc">
							<c:set var="seleccionado" value="${ opc.idCatalogoDetalle == ubicacion.ubicaciones[count].tipoInmueble ? 'selected' : '' }"></c:set>
							<option ${ seleccionado } value="${opc.idCatalogoDetalle}">${opc.valor}</option>
						</c:forEach>
					</select>
					<label for="tip_inm-${count}">
						<liferay-ui:message key="CotizadorPaso2Portlet.lblTipInm" />
					</label>
				</div>
			</div>
			<div class="col-sm-3">
				<div class="md-form form-group">
					<select name="tip_uso-${count}" id="tip_uso-${count}" class="mdb-select tip_uso infReq">
						<option value="-1" selected><liferay-ui:message key="CotizadorPaso2Portlet.selectOpDefoult" /></option>
						<c:forEach items="${listaUso}" var="opc">
							<c:set var="seleccionado" value="${ opc.idCatalogoDetalle == ubicacion.ubicaciones[count].tipoUso ? 'selected' : '' }"></c:set>
							<option ${ seleccionado } value="${opc.idCatalogoDetalle}">${opc.valor}</option>
						</c:forEach>
					</select>
					<label for="tip_uso-${count}">
						<liferay-ui:message key="CotizadorPaso2Portlet.lblTipUso" />
					</label>
				</div>
			</div>
			<div class="col-md-3">
				<div class="md-form form-group">
					<input type="text" name="dr_nivel-${count}" id="dr_nivel-${count}" class="form-control infReq niveles" value="${ ubicacion.ubicaciones[count].niveles != 0 ? ubicacion.ubicaciones[count].niveles : '' }" >
					<!--  select name="dr_nivel" id="dr_nivel" class="mdb-select form-control-sel  infReq">
						<option value="-1" selected><liferay-ui:message key="CotizadorPaso2Portlet.selectOpDefoult" /></option>
						<c:forEach items="${listaNiveles}" var="option">
							<c:set var="seleccionado" value="${ option.idCatalogoDetalle == ubicacion.ubicaciones[count].niveles ? 'selected' : '' }"></c:set>
							<option ${ seleccionado } value="${option.idCatalogoDetalle}">${option.descripcion}</option>
						</c:forEach>
					</select -->
					<label for="dr_nivel-${count}">
						<liferay-ui:message key="CotizadorPaso2Portlet.lblNivelesP2" />
					</label>
				</div>
			</div>
		</div>
		<div class="row" >
			<div class="col-md-9">
				<div class="row">
					<div class="col-md-4">
						<div class="md-form">
							<select name="dr_descTechos-${count}" id="dr_descTechos-${count}" class="mdb-select form-control-sel infReq tipoTechos">
								<option value="-1" selected><liferay-ui:message key="CotizadorPaso1Portlet.selectOpDefoult" /></option>
								<c:forEach items="${listaTechos}" var="option">
									<c:if test="${fn:contains(option.valor, 'MADERA')}">
										<c:if test="${perfilSuscriptor == 1 }">
									<option value="${option.idCatalogoDetalle}" ${ ubicacion.ubicaciones[count].techos == option.idCatalogoDetalle ? 'selected' : ''}>${option.valor}</option>
										</c:if>
									</c:if>
									<c:if test="${not fn:contains(option.valor, 'MADERA')}">
										<option value="${option.idCatalogoDetalle}" ${ ubicacion.ubicaciones[count].techos == option.idCatalogoDetalle ? 'selected' : ''}>${option.valor}</option>
									</c:if>
								</c:forEach>
							</select>
							<label for="dr_descTechos-${count}">
								<liferay-ui:message key="CotizadorPaso2Portlet.descTechos" />
							</label>
						</div>
					</div>
					<div class="col-md-4">
						<div class="md-form">
							<select name="dr_descMuros-${count}" id="dr_descMuros-${count}" class="mdb-select form-control-sel infReq">
								<option value="-1" selected><liferay-ui:message key="CotizadorPaso1Portlet.selectOpDefoult" /></option>
								<c:forEach items="${listaMuros}" var="option">
									<option value="${option.idCatalogoDetalle}" ${ ubicacion.ubicaciones[count].muros == option.idCatalogoDetalle ? 'selected' : ''}>${option.valor}</option>
								</c:forEach>
							</select>
							<label for="dr_descMuros-${count}">
								<liferay-ui:message key="CotizadorPaso2Portlet.descMuros" />
							</label>
						</div>
					</div>
					<div class="col-md-4">
						<div class="md-form">
							<select name="dr_descMedidasSeguridad-${count}" id="dr_descMedidasSeguridad-${count}" class="mdb-select form-control-sel descMedidasSeguridad infReq">
								<option value="-1" selected><liferay-ui:message key="CotizadorPaso1Portlet.selectOpDefoult" /></option>
								<c:forEach items="${listaMedidas}" var="option">
									<option value="${option.idCatalogoDetalle}" ${ ubicacion.ubicaciones[count].medidasSeguridad == option.idCatalogoDetalle ? 'selected' : ''}>${option.valor}</option>
								</c:forEach>
							</select>
							<label for="dr_descMedidasSeguridad-${count}">
								<liferay-ui:message key="CotizadorPaso2Portlet.descMedidasSeguridad" />
							</label>
						</div>
					</div>
				</div>
				<div class="row">
					<c:if test="${perfilSuscriptor == 1}">
					<div class="col-md-4">
						<div class="md-form form-group">
							<input type="text" name="dr_latitud-${count}" id="dr_latitud-${count}" class="form-control  longlat" value="${ ubicacion.ubicaciones[count].latitud != 0 ? ubicacion.ubicaciones[count].latitud : ''}">
							<label for="dr_latitud-${count}">
								<liferay-ui:message key="CotizadorPaso2Portlet.latitud" />
							</label>
						</div>
					</div>
					<div class="col-md-4">
						<div class="md-form form-group">
							<input type="text" name="dr_longitud-${count}" id="dr_longitud-${count}" class="form-control  longlat" value="${ ubicacion.ubicaciones[count].longitud != 0 ? ubicacion.ubicaciones[count].longitud : '' }" >
							<label for="dr_longitud-${count}">
								<liferay-ui:message key="CotizadorPaso2Portlet.longitud" />
							</label>
						</div>
					</div>
					</c:if>
					<div class="col-md-4">
						<div class="md-form form-group d-none">
							<input type="text" name="dr_nivelRiesgo-${count}" id="dr_nivelRiesgo-${count}" class="form-control nivelRiesgo" value="${ ubicacion.ubicaciones[count].nivelRiesgo != 0 ? ubicacion.ubicaciones[count].nivelRiesgo : '' }" >
							<label for="dr_nivelRiesgo-${count}">
								<liferay-ui:message key="CotizadorPaso2Portlet.nivelRiesgo" />
							</label>
						</div>
					</div>
				</div>
			</div>
			<div class="col-md-3">
				<div class="md-form d-none">
					<textarea id="dr_descOtros-${count}" name="dr_descOtros-${count}" class="md-textarea form-control" rows="3" maxlength="300" style="text-transform: uppercase;">${ ubicacion.ubicaciones[count].descripcionMedidaSeguridad }</textarea>
					<label for="dr_descOtros-${count}">
						<liferay-ui:message key="CotizadorPaso2Portlet.descOtros" />
					</label>
				</div>
			</div>
		</div>
		<div class="row">
			<div class="col-md-6">
				<div class="form-check">
					<label class="form-check-label" for="dr_descPregunta-${count}">
						<liferay-ui:message key="CotizadorPaso2Portlet.descPregunta" />
					</label>
				</div>
			</div>
			<div class="col-md-6">
				<div class="form-inline">
					<div class="form-check">
						<input class="form-check-input form-control infReq" name="group-ubi-${(count+1)}" type="radio" id="dr_descPreguntaSi-${count}" value="2" ${ ubicacion.ubicaciones[count].cercaDelMar == true ? 'checked' : '' }/>
						<label class="form-check-label" for="dr_descPreguntaSi-${count}">
							<liferay-ui:message key="CotizadorPaso2Portlet.descPreguntaSi" />
						</label>
					</div>
					<div class="form-check">
						<input class="form-check-input form-control infReq" name="group-ubi-${(count+1)}" type="radio" id="dr_descPreguntaNo-${count}" value="1" ${ ubicacion.ubicaciones[count].cercaDelMar == false ? 'checked' : '' }/>
						<label class="form-check-label" for="dr_descPreguntaNo-${count}">
							<liferay-ui:message key="CotizadorPaso2Portlet.descPreguntaNo" />
						</label>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>
