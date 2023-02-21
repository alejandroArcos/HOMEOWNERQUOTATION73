<%@ include file="./init.jsp"%>
<jsp:include page="modalesPaso2.jsp" />

<portlet:resourceURL id="/cotizadores/guardaubicaciones" var="guardaUbicacionURL" cacheability="FULL" />
<portlet:resourceURL id="/cotizadores/paso2/getCP" var="getCpURL" cacheability="FULL" />
<portlet:resourceURL id="/cotizadores/redirigePasoX" var="redirigeURL" cacheability="FULL" />
<portlet:resourceURL id="/cotizadores/paso2/aceptaSuscripcion" var="aceptaSuscripcionURL" cacheability="FULL" />
<portlet:resourceURL id="/paso2/sendMailAgenteSuscriptor" var="sendMailAgenteSuscriptorURL" cacheability="FULL" />
<portlet:resourceURL id="/cotizadores/paso2/guardaBajaEndoso" var="guardaBajaEndosoURL" />
<portlet:resourceURL id="/cotizadores/paso1/listaPersonas" var="listaPersonasURL" cacheability="FULL" />

<portlet:actionURL var="cotizadorActionPaso3" name="/cotizadores/actionPaso3" />
<portlet:actionURL var="cotizadorActionPaso1" name="/cotizadores/actionPaso1" />

<c:set var="listaCatDenominacion" scope="session" value="${listaCatDenominacion}" />


<link rel="stylesheet" href="<%=request.getContextPath()%>/css/main.css?v=${version}">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/jquery-ui.css?v=${version}">

<fmt:setLocale value="es_MX" />

<section id="cotizadores-p2" class="upper-case-all">
	<div class="section-heading">
		<div class="container-fluid">
			<h4 class="title text-left">${tituloCotizador}</h4>
		</div>
	</div>
	<div class="container-fluid">
		<div class="row">
			<div class="col-md-12">
				<ul class="stepper stepper-horizontal container-fluid">
					<li id="step1" class="completed">
						<a href="javascript:void(0)">
							<span class="circle">1</span>
							<span class="label">
								<liferay-ui:message key="CotizadorPaso2Portlet.titPasoUno" />
							</span>
						</a>
					</li>
					<li id="step2" class="active">
						<a href="javascript:void(0)">
							<span class="circle">2</span>
							<span class="label">
								<liferay-ui:message key="CotizadorPaso2Portlet.titPasoDos" />
							</span>
						</a>
					</li>
					<li id="step3">
						<a href="javascript:void(0)">
							<span class="circle">3</span>
							<span class="label">
								<liferay-ui:message key="CotizadorPaso2Portlet.titPasoTres" />
							</span>
						</a>
					</li>
					<li id="step4">
						<a href="javascript:void(0)">
							<span class="circle">4</span>
							<span class="label">
								<liferay-ui:message key="CotizadorPaso2Portlet.titPasoCuatro" />
							</span>
						</a>
					</li>
				</ul>

			</div>
		</div>
	</div>
	<div style="position: relative;">
		<liferay-ui:success key="consultaExitosa" message="CotizadorPaso2Portlet.exito" />
		<liferay-ui:error key="errorConocido" message="${errorMsg}" />
		<liferay-ui:error key="errorDesconocido" message="CotizadorPaso2Portlet.erorDesconocido" />
	</div>
	<div class="padding70" id="contenPaso2">
		<div class="container-fluid paso2Datos">
			<div class="row">
				<div class="col-md-9">
					${nombreContratante}
				</div>
				<div class="col-md-3" style="text-align: right;">
					<div class="md-form form-group">
						<input id="txtFolioP2_2" type="text" name="txtFolioP2_2" class="form-control" disabled value="${ folioCotizacion} - ${ versionCotizacion}">
						<label for="txtFolioP2_2">
							<liferay-ui:message key="CotizadorPaso2Portlet.titFolio" />
						</label>
					</div>
				</div>
			</div>
			<div id="chkBjaEnd" class="row d-none">
				<div class="col-md-12">
					<div class="text-center">
						<h2>
							Seleccione las ubicaciones que quiere dar de baja
						</h2>
						<br>
						<c:forEach var="j" begin="1" end="${ infCotizacion.noUbicaciones }">
							<div class="form-check form-check-inline">
							  <input type="checkbox" class="form-check-input"
							   id="chk-${ j }" ${ j == 1 ? 'disabled' : '' } >
							  <label class="form-check-label" for="chk-${ j }">${relacionUbicaciones[j]}</label>
							</div>
						</c:forEach>
					</div>
				</div>
			</div>
			<div class="row">
				<div class="col-md-12">
					<button id="btn-add-tab-endosos" type="button" class="btn btn-blue btn-sm waves-effect waves-light pull-right">Agregar Ubicación</button>
				</div>
				<div class="col-md-12">
					<div id="tabs">

						<ul>
							<c:forEach var="i" begin="1" end="${ infCotizacion.noUbicaciones }">
								<li id="tab-${i}">
									<a href="#ubicacion-${i}">
										<i class="fa fa-map-marker" aria-hidden="true"></i>
										<span class="numUbicacionEndo">${relacionUbicaciones[i]}</span>
									</a>
									<button id="btnCls${i}" type="button" class="close my-1 mr-2" aria-label="Close">
										<span aria-hidden="true">×</span>
									</button>
								</li>
							</c:forEach>
						</ul>
						<c:forEach var="i" begin="1" end="${ infCotizacion.noUbicaciones }">
						<%-- c:set var="i" value="1"/ --%>
							<div id="ubicacion-${i}" style="padding: 50px;" class="ubicacion">
								<div class="row">
									<div class="col-md-12">
										<c:set var="count" value="${ i - 1 }" scope="request"></c:set>
										<jsp:include page="tab_Familiar.jsp" />			
										<div class="row">
											<div class="col-md-12 acordeon" id="acordeon${ i }">
												${jsonFiels[i]}
											</div>
										</div>
									</div>
								</div>
							</div>
						</c:forEach>
					</div>
				</div>
			</div>
			<div class="row mt-4">
				<div  class="col-md-8">
					<c:if test="${perfilSuscriptor == 1}">
						<div id="infoPrimas" class="row">
							<table class="table tablaTit" id="tabla_Tittotal">
								<thead class="blue-grey lighten-4">
									<tr class="triTabTot">
										<th>
											<liferay-ui:message key="CotizadorPaso2Portlet.lblUbicacionP2" />
										</th>
									</tr>
								</thead>
								<tbody>
									<tr class="trsTabParcial">
										<td scope="row">
											<liferay-ui:message key="CotizadorPaso2Portlet.lblPrimaNetaP2" />
										</td>
									</tr>
								</tbody>
							</table>
							<div class="table-wrapper-scroll-table">
								<table class="table tablaTotal" id="tabla_total" >
									<thead class="blue-grey lighten-4">
										<tr class="triTabTot">
											<c:forEach items="${ ubicacion.ubicaciones }" var="u">
												<th>${ u.idubicacion }</th>
											</c:forEach>
										</tr>
									</thead>
									<tbody>
										<tr class="trsTabParcial">
											<c:forEach items="${ ubicacion.ubicaciones }" var="u">
												<td><fmt:formatNumber value="${u.primaNeta}" type="currency"  /> </td>
											</c:forEach>
										</tr>
									</tbody>
								</table>
							</div>
							<table class="table col-md-4 tablaTotal2" id="tabla_total_2">
								<thead class="blue-grey lighten-4">
									<tr>
										<th id="tit_table">
											<liferay-ui:message key="CotizadorPaso2Portlet.lblPrimaTotalP2" />
										</th>
									</tr>
								</thead>
								<tbody>
									<tr>
										<td id="tol_table" class="totPrimaTab"> 
											<fmt:formatNumber value="${ubicacion.primaTotal}" type="currency" />
										</td>
									</tr>
								</tbody>
							</table>
						</div>
					</c:if>
				</div>
				<div class="col-md-4">
					<div class="row">
						<div class="col-md-12">
							<a class="btn btn-blue waves-effect waves-light pull-right btn-ubicacion " id="save_tot2">
								<liferay-ui:message key="CotizadorPaso2Portlet.btnGuardarUbicaP2" />
							</a>
						</div>
					</div>
					<div class="row">
						<div class="col-md-12">
							<!-- 							<a class="btn btn-pink waves-effect waves-light btn-ubicacion d-none" id="paso2_suscripcion_next" > -->
							<%-- 								<liferay-ui:message key="CotizadorPaso2Portlet.btnContinuarP1" /> --%>
							<!-- 							</a> -->
							<!-- a class="btn btn-pink waves-effect waves-light btn-ubicacion pull-right" id="paso2_next">
								<liferay-ui:message key="CotizadorPaso2Portlet.btnContinuarP3" />
							</a-->
							<form action="${cotizadorActionPaso3}" method="post" id="paso2-form">
								<input type='hidden' id="infoCotizacion" class="infoCot" name="infoCotizacion" value="" />
								<button type='button' class="btn btn-pink waves-effect waves-light btn-ubicacion pull-right" id="paso2_next">
									<liferay-ui:message key="CotizadorPaso2Portlet.btnContinuarP3" />
								</button>
							</form>
							<form action="${cotizadorActionPaso3}" method="post" id="paso2-form-next">
								<input type='hidden' id="infoCotizacion" class="infoCot" name="infoCotizacion" value="" />
								<button type='button' class="btn btn-pink waves-effect waves-light btn-ubicacion pull-right d-none" id="paso2_next_paso3">
									<liferay-ui:message key="CotizadorPaso2Portlet.btnContinuarP3" />
								</button>
							</form>
							<a class="btn btn-blue waves-effect waves-light btn-ubicacion pull-right" data-toggle="modal" data-target="#modalRegresarP1" id="paso1_back2">
								<liferay-ui:message key="CotizadorPaso2Portlet.btnRegresarP2" />
							</a>
							<a class="btn btn-blue waves-effect waves-light btn-ubicacion pull-right d-none" id="regresarEndoso" onclick="regresaPaso1();">
								<liferay-ui:message key="CotizadorPaso2Portlet.btnRegresarP2" />
							</a>
							<a class="btn btn-pink waves-effect waves-light btn-ubicacion d-none pull-right" id="con_baja">
								<liferay-ui:message key="CotizadorPaso2Portlet.btnContinuarP3" />
							</a>
							<a class="btn btn-pink waves-effect waves-light btn-ubicacion  d-none pull-right" id="btnsuscontinuar"> A SUSCRIPCIÓN </a>
							<form action="${cotizadorActionPaso1}" method="post" id="paso2-form-back">
								<input type='hidden' id="infoCotizacionBack" name="infoCotizacionBack" value="" />
							</form>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</section>



<script src="<%=request.getContextPath()%>/js/jquery-ui.min.js?v=${version}"></script>
<script src="<%=request.getContextPath()%>/js/objetos.js?v=${version}"></script>
<script src="<%=request.getContextPath()%>/js/funcionesGenericas.js?v=${version}"></script>
<script src="<%=request.getContextPath()%>/js/visibilidad.js?v=${version}"></script>
<script src="<%=request.getContextPath()%>/js/mainPaso2.js?v=${version}"></script>


<script>
 var infCotString  = '${infCotJson}';
 
 var guardaUbicacionURL = '${guardaUbicacionURL}';
 var getCpURL = '${getCpURL}';
 var redirigeURL = '${redirigeURL}';
 var aceptaSuscripcionURL = '${aceptaSuscripcionURL}';
 var sendMailAgenteSuscriptorURL = '${sendMailAgenteSuscriptorURL}';
 var guardaBajaEndosoURL = '${guardaBajaEndosoURL}';
 
 var asegurados = ${relacionAsegurados};
 var beneficiarios = ${relacionBeneficiarios};
 
 var subGiroRiesgo = '${subGiroRiesgo}';
 var excedeLimites = '${excedeLimites}';
 var aceptaSuscripcion = '${aceptaSuscripcion}';
 var perfilSuscriptor = '${perfilSuscriptor}';
 var perfilJapones = '${perfilJapones}';
 var infoP1 = '${infoP1}';
 var contadorAsegurados = [];
 var contadorBeneficiarios = [];
 var idConsecutivoAseg = [];
 var idConsecutivoBenef = [];
 
 var aseguradoAdicionalURL = "<%=request.getContextPath()%>/paso2/aseguradoAdicional.jsp";
 var beneficiarioPreferenteURL = "<%=request.getContextPath()%>/paso2/beneficiarioPreferente.jsp";
 
 
 ligasServicios.listaPersonas = "${listaPersonasURL}";
 
 var infoCargaMasiva = ${infoCargaMasiva};
 var erroresCargaMasiva = 0;
 
</script>