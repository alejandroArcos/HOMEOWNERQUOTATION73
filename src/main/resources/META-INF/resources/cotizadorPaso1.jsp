<%@ include file="./init.jsp"%>
<jsp:include page="modales.jsp" />

<portlet:resourceURL id="/cotizadores/paso1/listaPersonas" var="listaPersonasURL" cacheability="FULL" />
<portlet:resourceURL id="/cotizadores/paso1/getSubGiro" var="getSubGiroURL" />
<portlet:resourceURL id="/cotizadores/paso1/guardaPaso" var="guardaPaso1URL" cacheability="FULL" />
<portlet:resourceURL id="/cotizadores/redirigePasoX" var="redirigeURL" />
<portlet:resourceURL id="/cotizadores/paso1/canalNegocio" var="canalNegocio" cacheability="FULL" />
<portlet:resourceURL id="/cotizadores/paso1/cargaMasiva" var="cargaMasiva" cacheability="FULL" />
<portlet:resourceURL id="/cotizadores/validaAgente" var="validaAgenteURL" cacheability="FULL" />

<portlet:resourceURL id="/cotizadores/paso3/getComisionesAgente" var="getComisionesAgenteURL" />
<portlet:resourceURL id="/cotizadores/paso3/guardaComisionesAgente" var="guardaComisionesAgenteURL" />

<portlet:actionURL var="cotizadorActionPaso2" name="/cotizadores/actionPaso2" />
<portlet:actionURL var="cotizadorActionPaso3" name="/cotizadores/actionPaso3" />



<link rel="stylesheet" href="<%=request.getContextPath()%>/css/main.css?v=${version}">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/jquery-ui.css?v=${version}">

<section id="cotizadores-p1" class="upper-case-all">

	<div class="section-heading">
		<div class="container-fluid">
			<h4 class="title text-left">${tituloCotizador}</h4>
		</div>
	</div>
	<div class="container-fluid">
		<div class="row">
			<div class="col-md-12">
				<ul class="stepper stepper-horizontal container-fluid">
					<li id="step1" class="active ">
						<a href="javascript:void(0)">
							<span class="circle">1</span>
							<span class="label">
								<liferay-ui:message key="CotizadorPaso1Portlet.titPasoUno" />
							</span>
						</a>
					</li>
					<li id="step2">
						<a href="javascript:void(0)">
							<span class="circle">2</span>
							<span class="label">
								<liferay-ui:message key="CotizadorPaso1Portlet.titPasoDos" />
							</span>
						</a>
					</li>
					<li id="step3">
						<a href="javascript:void(0)">
							<span class="circle">3</span>
							<span class="label">
								<liferay-ui:message key="CotizadorPaso1Portlet.titPasoTres" />
							</span>
						</a>
					</li>
					<li id="step4">
						<a href="javascript:void(0)">
							<span class="circle">4</span>
							<span class="label">
								<liferay-ui:message key="CotizadorPaso1Portlet.titPasoCuatro" />
							</span>
						</a>
					</li>
				</ul>

			</div>
		</div>
	</div>

	<div style="position: relative;">
		<liferay-ui:success key="consultaExitosa" message="CotizadorPaso1Portlet.exito" />
		<liferay-ui:error key="errorConocido" message="${errorMsg}" />
		<liferay-ui:error key="errorDesconocido" message="CotizadorPaso1Portlet.erorDesconocido" />
	</div>

	<div class="container-fluid" id="divPaso1">
		<div class="row">
			<div class="col-md-6"></div>
			<c:if test="${numeroSolicitud}">
				<div class="col-md-3" style="text-align: right;">
					<div class="md-form form-group">
						<input id="txtSolicitud" type="text" name="txtSolicitud" class="form-control" value="${ inf.solicitud }" disabled>
						<label class="active" for="txtSolicitud">
							<liferay-ui:message key="CotizadorPaso1Portlet.titSolicitud" />
						</label>
					</div>
				</div>
			</c:if>
			<div class="col-md-3 divFolio" style="text-align: right;">
				<div class="md-form form-group">
					<input id="txtFolioP1" type="text" name="txtFolioP1" class="form-control" value="${ inf.folio } - ${ inf.version }" disabled>
					<label class="active" for="txtFolioP1">
						<liferay-ui:message key="CotizadorPaso1Portlet.titFolio" />
					</label>
				</div>
			</div>
		</div>
	</div>

	<div class="padding70" id="contPaso1">

		<h5 id="titCotizaFamiliar" class="d-none">
			<liferay-ui:message key="CotizadorPaso1Portlet.titDatContratante" />
			<br />
		</h5>

		<div class="col-md-12">
			<!--Accordion wrapper-->
			<div class="accordion md-accordion" id="accordionEx" role="tablist" aria-multiselectable="true">


				<!-- Accordion card -->
				<div class="card ">


					<!-- Card header -->
					<div class="card-header btn-blue modificado d-none" role="tab" id="headingDatosContratante">
						<a class="collapsed" data-toggle="collapse" data-parent="#accordionEx" href="#collapseDatosContratante" aria-expanded="false" aria-controls="collapseDatosContratante">
							<h5 class="mb-0">
								<liferay-ui:message key="CotizadorPaso1Portlet.titDatContratante" />
								<i class="fas fa-angle-down rotate-icon"></i>
							</h5>
						</a>
					</div>

					<div id="collapseDatosContratante" class="collapse in" role="tabpanel" aria-labelledby="headingDatosContratante" data-parent="#accordionEx">
						<div class="card-body">

							<div class="row">
								<div class="col-md-12">
									<div class="form-inline divRdoTpClient">
										<div class="form-check">
											<input class="form-check-input form-control" name="group1" type="radio" id="radio_ce" value="0" checked="checked">
											<label class="form-check-label" for="radio_ce">
												<liferay-ui:message key="CotizadorPaso1Portlet.rdbCliExP1" />
											</label>
										</div>
										<div class="form-check">
											<input class="form-check-input form-control" name="group1" type="radio" id="radio_cn" value="1">
											<label class="form-check-label" for="radio_cn">
												<liferay-ui:message key="CotizadorPaso1Portlet.rdbCliNuP1" />
											</label>
										</div>
									</div>
								</div>
							</div>

							<div class="row data_cteext">
								<div class="col-md-3">
									<div class="md-form">
										<input type="text" name="ce_rfc" id="ce_rfc" class="form-control " maxlength="13" pattern="^[a-zA-Z0-9]{4,10}$" value="${ cotizadorData.datosCliente.rfc }">
										<label for="ce_rfc">
											<liferay-ui:message key="CotizadorPaso1Portlet.lblRfcExP1" />
										</label>
									</div>
								</div>
								<div class="col-md-6">
									<div class="md-form">
										<input type="text" name="ce_nombre" id="ce_nombre" class="form-control "
											value="${ fn:escapeXml(cotizadorData.datosCliente.nombre) } ${ cotizadorData.datosCliente.appPaterno} ${ cotizadorData.datosCliente.appMaterno }">
										<label for="ce_nombre">
											<liferay-ui:message key="CotizadorPaso1Portlet.lblNomComExP1" />
										</label>
									</div>
								</div>
								<div class="col-md-3">
									<div class="md-form">
										<input type="text" name="ce_codigo" id="ce_codigo" class="form-control" value="${ cotizadorData.datosCliente.codigo }" disabled>
										<label for="ce_codigo">
											<liferay-ui:message key="CotizadorPaso1Portlet.lblCodClieExP1" />
										</label>
									</div>
								</div>
							</div>


							<div class="row data_ctenvo d-none">
								<div class="col-sm-12">
									<div class="row data_nuevotip">
										<div class="col-md-8 cn_ncEx">
											<div class="md-form form-group">
												<input type="text" id="cn_nombrecompleto" name="cn_nombrecompleto" class="form-control" disabled>
												<label for="cn_nombrecompleto">
													<liferay-ui:message key="CotizadorPaso1Portlet.lblNomComExP1" />
												</label>
											</div>
										</div>
										<div class="col-md-4 cn_tpEx">
											<div class="form-inline tipo_persona">
												<div class="form-check">
													<input class="form-check-input form-control" name="group2" type="radio" id="cn_personamoral" checked="checked" value="2">
													<label class="form-check-label" for="cn_personamoral">
														<liferay-ui:message key="CotizadorPaso1Portlet.lblTipPerNvMorP1" />
													</label>
												</div>
												<div class="form-check">
													<input class="form-check-input form-control" name="group2" type="radio" id="cn_personafisica" value="1">
													<label class="form-check-label" for="cn_personafisica">
														<liferay-ui:message key="CotizadorPaso1Portlet.lblTipPerNvFisP1" />
													</label>
												</div>
											</div>
										</div>

										<div class="col-md-3 cn_rdEx d-none">
											<div class="row row justify-content-md-center">
												<label class="pb-2"> Extranjera:</label>
											</div>
											<div class="row row justify-content-md-center">
												<div class="switch">
													<label>
														No
														<input id="chktoggle" type="checkbox">
														<span class="lever"></span>
														Si
													</label>
												</div>
											</div>
										</div>
									</div>
									<div class="row">
										<div class="col-md-3">
											<div class="md-form">
												<input type="text" id="cn_rfc" name="cn_rfc " class="form-control " maxlength="13" pattern="^[a-zA-Z0-9]{4,10}$">
												<label for="cn_rfc">
													<liferay-ui:message key="CotizadorPaso1Portlet.lblRfcExP1" />
												</label>
											</div>
										</div>

										<div class="col-md-9 px-0 tip_moral divPerMor">
											<div class="col-md-6">
												<div class="md-form">
													<input type="text" id="cn_nombrecontratante" name="cn_nombrecontratante" class="form-control">
													<label for="cn_nombrecontratante">
														<liferay-ui:message key="CotizadorPaso1Portlet.lblNomConNvMoP1" />
													</label>
												</div>
											</div>
											<div class="col-md-6">
												<div class="md-form form-group">
													<select name="cn_denominacion" id="cn_denominacion" class="mdb-select form-control-sel colorful-select dropdown-primary"
														searchable='<liferay-ui:message key="CotizadorPaso1Portlet.buscar" />'>
														<option value="-1" selected><liferay-ui:message key="CotizadorPaso1Portlet.selectOpDefoult" /></option>
														<c:forEach items="${listaCatDenominacion}" var="option">
															<option value="${option.idCatalogoDetalle}">${option.valor}</option>
														</c:forEach>
													</select>
													<label for="cn_denominacion">
														<liferay-ui:message key="CotizadorPaso1Portlet.lblDenominaNvMoP1" />
													</label>
												</div>
											</div>
										</div>



										<div class="col-md-9 px-0 tip_fisica" style="display: none">

											<div class="col-md-4">
												<div class="md-form">
													<input type="text" id="cn_fisnombre" name="cn_fisnombre" class="form-control">
													<label for="cn_fisnombre">
														<liferay-ui:message key="CotizadorPaso1Portlet.lblNomFisicaP1" />
													</label>
												</div>
											</div>
											<div class="col-md-4">
												<div class="md-form">
													<input type="text" id="cn_fispaterno" name="cn_fispaterno" class="form-control ">
													<label for="cn_fispaterno">
														<liferay-ui:message key="CotizadorPaso1Portlet.lblApPatFisicaP1" />
													</label>
												</div>
											</div>
											<div class="col-md-4">
												<div class="md-form">
													<input type="text" id="cn_fismaterno" name="cn_fismaterno" class="form-control ">
													<label for="cn_fismaterno">
														<liferay-ui:message key="CotizadorPaso1Portlet.lblApMatFisicaP1" />
													</label>
												</div>
											</div>
										</div>
									</div>
								</div>
							</div>

							<div class="data_cotizacion">
								<br />
								<h5>
									<liferay-ui:message key="CotizadorPaso1Portlet.titDatosCotizacion" />
								</h5>
								<br />
								<div class="row">
									<div class="col-md-12">
										<div class="form-inline form-right pull-right divRdoVigencia">
											<div class="form-check">
												<input class="form-check-input form-control" name="group3" type="radio" id="dc_cotizarVig" checked="checked" value="0">
												<label class="form-check-label" for="dc_cotizarVig">
													<liferay-ui:message key="CotizadorPaso1Portlet.rdbCotVigDtsCotizaP1" />
												</label>
											</div>
											<div class="form-check">
												<input class="form-check-input form-control" name="group3" type="radio" id="dc_vigenAnual" value="1">
												<label class="form-check-label" for="dc_vigenAnual">
													<liferay-ui:message key="CotizadorPaso1Portlet.rdbVigAnualDtsCotizaP1" />
												</label>
											</div>
										</div>
									</div>
								</div>

								<div class="row">
									<div class="col-sm-3">
										<div class="md-form form-group">
											<select name="dc_movimientos" id="dc_movimientos" class="mdb-select form-control-sel colorful-select dropdown-primary">
												<option value="-1" selected><liferay-ui:message key="CotizadorPaso1Portlet.selectOpDefoult" /></option>
												<c:forEach items="${listaMovimiento}" var="option">
													<option value="${option.idCatalogoDetalle}" ${ (cotizadorData.tipoMov ==  option.idCatalogoDetalle) || (option.idCatalogoDetalle == 207) ? 'selected' : ''}>${option.valor}</option>
												</c:forEach>
											</select>
											<label for="dc_movimientos">
												<liferay-ui:message key="CotizadorPaso1Portlet.lblTipoMovimientoDtsCotizaP1" />
											</label>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="md-form form-group">
											<select name="dc_moneda" id="dc_moneda" class="mdb-select form-control-sel">
												<option value="-1" selected><liferay-ui:message key="CotizadorPaso1Portlet.selectOpDefoult" /></option>
												<c:forEach items="${listaCatMoneda}" var="option">
													<option value="${option.idCatalogoDetalle}" ${ cotizadorData.moneda ==  option.idCatalogoDetalle ? 'selected' : ''}>${option.valor}</option>
												</c:forEach>
											</select>
											<label for="dc_moneda">
												<liferay-ui:message key="CotizadorPaso1Portlet.lblMonedaDtsCotizaP1" />
											</label>
										</div>
									</div>
									<div class="col-sm-6">
										<div class="md-form form-group">
											<div class="row">
												<div class="col">
													<input placeholder="Fecha Desde" type="date" id="dc_dateDesde" name="dc_dateDesde" class="form-control datepicker " value="${ fechaHoy }">
													<label for="dc_dateDesde">
														<liferay-ui:message key="CotizadorPaso1Portlet.lblDesdeDtsCotizaP1" />
													</label>
												</div>
												<div class="col">
													<input placeholder="Fecha Hasta" type="date" id="dc_dateHasta" name="dc_dateHasta" class="form-control datepicker" value="${ fechaMasAnio }" disabled>
													<label for="dc_dateHasta">
														<liferay-ui:message key="CotizadorPaso1Portlet.lblHastaDtsCotizaP1" />
													</label>
												</div>
											</div>
										</div>
									</div>
								</div>
								<div class="row">
									<div class="col-sm-3">
										<div class="md-form form-group">
											<select name="dc_agentes" id="dc_agentes" class="mdb-select form-control-sel colorful-select dropdown-primary" searchable='<liferay-ui:message key="CotizadorPaso1Portlet.buscar" />'>
												<c:if test="${fn:length(listaAgentes) gt 1}">
													<option value="-1" selected><liferay-ui:message key="CotizadorPaso1Portlet.selectOpDefoult" /></option>
												</c:if>
												<c:forEach items="${listaAgentes}" var="option">
													<option value="${option.idPersona}" ${ cotizadorData.agente ==  option.idPersona ? 'selected' : ''}>${option.nombre}${option.appPaterno}${option.appMaterno}</option>
												</c:forEach>
											</select>
											<label for="dc_agentes">
												<liferay-ui:message key="CotizadorPaso1Portlet.lblAgentesDtsCotizaP1" />
											</label>
										</div>
									</div>

									<div class="col-sm-3">
										<div class="md-form form-group">
											<select name="dc_formpago" id="dc_formpago" class="mdb-select form-control-sel ">
												<option value="-1" selected><liferay-ui:message key="CotizadorPaso1Portlet.selectOpDefoult" /></option>
												<c:forEach items="${listaCatFormaPago}" var="option">
													<c:if test="${ option.codigo == 'S' }">
														<c:if test="${ perfilSuscriptor == 1 || perfilJapones == 1}">
															<option value="${option.idCatalogoDetalle}" ${ cotizadorData.formaPago ==  option.idCatalogoDetalle ? 'selected' : ''}>${option.valor}</option>
														</c:if>
													</c:if>
													<c:if test="${ option.codigo != 'S' }">
														<option value="${option.idCatalogoDetalle}" ${ cotizadorData.formaPago ==  option.idCatalogoDetalle ? 'selected' : ''}>${option.valor}</option>
													</c:if>
												</c:forEach>
											</select>
											<label for="dc_formpago">
												<liferay-ui:message key="CotizadorPaso1Portlet.lblPagoDtsCotizaP1" />
											</label>
										</div>
									</div>
									<div class="col-sm-3 d-none" id="div_canal">
										<div class="md-form form-group">
											<select name="dc_canalneg" id="dc_canalneg" class="mdb-select form-control-sel">
												<option value="-1" selected><liferay-ui:message key="CotizadorPaso1Portlet.selectOpDefoult" /></option>
												<c:forEach items="${listaCatCanalNegocio}" var="option">
													<option value="${option.idCatalogoDetalle}" ${ cotizadorData.canalNegocio ==  option.idCatalogoDetalle ? 'selected' : ''}>${option.valor}</option>
												</c:forEach>
											</select>
											<label for="div_canal">
												<liferay-ui:message key="CotizadorPaso1Portlet.lblCanalNegocioP1" />
											</label>
										</div>
									</div>
									<div class="col-sm-3 d-none" id="div_coaseguro">
										<div class="md-form form-group">
											<select name="dc_coaseguro" id="dc_coaseguro" class="mdb-select form-control-sel">
												<option value="-1" selected><liferay-ui:message key="CotizadorPaso1Portlet.selectOpDefoult" /></option>
												<c:forEach items="${listaCatCoaseguro}" var="option">
													<c:if test="${option.codigo != '4'}">
														<option value="${option.idCatalogoDetalle}" ${ ((2575 == option.idCatalogoDetalle) && (perfilSuscriptor == 0) && (perfilJapones == 0)) || (cotizadorData.coaseguro ==  option.idCatalogoDetalle)  ? 'selected' : ''}>${option.valor}</option>
													</c:if>
												</c:forEach>
											</select>
											<label for="dc_coaseguro">
												<liferay-ui:message key="CotizadorPaso1Portlet.lblCoaseguroP1" />
											</label>
										</div>
									</div>
									<div class="col-sm-3 ${idPerfilUser == 4 || idPerfilUser == 5 || idPerfilUser == 6 ? '': 'd-none'}">
										<div class="md-form form-group">
											<select name="dc_sector" id="dc_sector" class="mdb-select form-control-sel ">
												<option value="-1" selected><liferay-ui:message key="CotizadorPaso1Portlet.selectOpDefoult" /></option>
												<c:forEach items="${listaCatSector}" var="option">
													<option value="${option.idCatalogoDetalle}" ${ cotizadorData.sector ==  option.idCatalogoDetalle ? 'selected' : ''} 
																								${ cotizadorData==null && option.valor=='PRIVADO' ? 'selected' : ''} >${option.valor}</option>												
												</c:forEach>
											</select>
											<label for="dc_sector">
												<liferay-ui:message key="CotizadorPaso1Portlet.lblSectorDtsCotizaP1" />
											</label>
										</div>
									</div>
									<div class="col-md-3">
											<div class="md-form">
												<input type="text" id="dac_tpoCambio" name="dac_tpoCambio" class="form-control" value="${tpoCambio}" readonly="readonly">
												<label for="dac_tpoCambio">
													<liferay-ui:message key="CotizadorPaso1Portlet.dac.tipCamb" />
												</label>
											</div>
										</div>
								</div>
								<div class="row" id="divCM">
									<div class="col-sm-3">
										<div class="md-form">
											<input type="text" id="cn_UB_Cotizar" name="cn_UB_Cotizar" class="form-control UB_Coti" value= ${ cotizadorData.p_noUbicaciones }>
											<label for="cn_UB_Cotizar">
												<liferay-ui:message key="CotizadorPaso1Portlet.lblUbicaACotizar" />
											</label>
										</div>
									</div>
									<div class="col-sm-3">
										<button id="btnCargMasi" class="btn btn-pink d-none">Carga Masiva</button>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>

			<!-- hata aqui los acordeones -->


			<div class="row">
				<div class="col-sm-12 text-right">
					<form class="mb-4" action="${cotizadorActionPaso2}" method="post" id="paso1_form">
						<input type='hidden' id="infoCotizacion" name="infoCotizacion" value="" /> 
						<div class="btn btn-pink" id="paso1_next">Continuar</div>
					</form>
					<form action="${cotizadorActionPaso3}" method="post" id="paso2-form-next">
						<input type='hidden' id="infoCotizacion" class="infoCot" name="infoCotizacion" value="" />
					</form>
				</div>
			</div>
		</div>
	</div>
</section>

<!-- 	Scripts -->

<script src="<%=request.getContextPath()%>/js/jquery-ui.min.js?v=${version}"></script>
<script src="<%=request.getContextPath()%>/js/main.js?v=${version}"></script>
<script src="<%=request.getContextPath()%>/js/objetos.js?v=${version}"></script>
<script src="<%=request.getContextPath()%>/js/funcionesGenericas.js?v=${version}"></script>



<script>
	ligasServicios.listaPersonas = "${listaPersonasURL}";
	ligasServicios.listaSubgiros = "${getSubGiroURL}";
	ligasServicios.guardaInfo = "${guardaPaso1URL}";
	ligasServicios.redirige = "${redirigeURL}";
	ligasServicios.canalNegocio = "${canalNegocio}";
	ligasServicios.cargaMasiva = "${cargaMasiva}";

	var infCotizacion = ${infCotizacionJson};
	var esRetroactivo = ${perfilMayorEjecutivo};
	var diasRetro = ${retroactividad};

	var datosCliente = '${datosClienteJSON}';
	var infVigencia = '${cotizadorData.vigencia}';
	var infsubEstado = '${cotizadorData.subEstado}';
	var perfilSuscriptor = '${perfilSuscriptor}';
	var perfilJapones = '${perfilJapones}';
	var puedeVerP1 = '${puedeVerP1}';
	var puedeEditarP1_1 = "${fn:containsIgnoreCase(P1_1.p_puedeModificarInfo, 'si')}";
	
	var validaAgenteURL = '${validaAgenteURL}';
	
	var cargaMasiva = '${cargaMasiva}';
	var cargaMasivaString = '';
</script>


