<%@ include file="../init.jsp"%>

<div class='row'>
	<div class='col-md-10'>
		<div class='col-md-9'>
			<div class='form-inline tipo_persona_paso2_bp'>
				<div class='form-check'>
					<input class='form-check-input form-control' name='groupBeneficiario' type='radio' id='radio_pm_bp' value='0'>
					<label class='form-check-label' for='radio_pm_bp'>
						<liferay-ui:message key="CotizadorPaso1Portlet.lblTipPerNvMorP1" />
					</label>
				</div>
				<div class='form-check'>
					<input class='form-check-input form-control' name='groupBeneficiario' type='radio' id='radio_pf_bp' value='1'>
					<label class='form-check-label' for='radio_pf_bp'>
						<liferay-ui:message key="CotizadorPaso1Portlet.lblTipPerNvFisP1" />
					</label>
				</div>
			</div>
		</div>
		<div class='col-md-3 tip_fisica_bp' style='display: none'>
			<div class='row row justify-content-md-center'>
				<label class='pb-2'> Extranjera:</label>
			</div>
			<div class='row row justify-content-md-center'>
				<div class='switch'>
					<label>
						No
						<input id='bp_chktoggle' type='checkbox' class="extranjera">
						<span class='lever'></span>
						Si
					</label>
				</div>
			</div>
		</div>
	</div>
	<div class='col-md-1 infoPersona d-none'>
		<a class="agregaBenef" style="color: #1976d2;"> <i class='fa fa-plus-circle' aria-hidden='true'></i></a>
	</div>
	<div class='col-md-1 infoPersona d-none'>
		<a class="eliminaBenef" style="color: #ec407a;"> <i class='far fa-times-circle' aria-hidden='true'></i></a>
	</div>
</div>
<div class='row infoPersona d-none'>
	<div class='col-md-3'>
		<div class='md-form'>
			<input type='text' id='bp_rfc' name='bp_rfc ' class='form-control rfc' maxlength='13' pattern='^[a-zA-Z0-9]{4,10}$'>
			<label for='bp_rfc'>
				<liferay-ui:message key="CotizadorPaso1Portlet.lblRfcExP1" />
			</label>
		</div>
	</div>
	<div class='col-md-9 px-0 tip_moral_bp divPerMor'>
		<div class='col-md-6'>
			<div class='md-form'>
				<input type='text' id='bp_nombrecontratante' name='bp_nombrecontratante' class='form-control nombreM'>
				<label for='bp_nombrecontratante'>
					<liferay-ui:message key="CotizadorPaso1Portlet.lblNomConNvMoP1" />
				</label>
			</div>
		</div>
		<div class='col-md-6'>
			<div class='md-form form-group'>
				<select name='bp_denominacion' id='bp_denominacion' class='mdb-select form-control-sel colorful-select dropdown-primary tipoS'
					searchable='<liferay-ui:message key="CotizadorPaso1Portlet.buscar" />'>
					<option value="-1" selected><liferay-ui:message key="CotizadorPaso1Portlet.selectOpDefoult" /></option>
					<c:forEach items="${listaCatDenominacion}" var="option">
						<option value="${option.idCatalogoDetalle}">${option.valor}</option>
					</c:forEach>
				</select>
				<label for='bp_denominacion'>
					<liferay-ui:message key="CotizadorPaso1Portlet.lblDenominaNvMoP1" />
				</label>
			</div>
		</div>
	</div>
	<div class='col-md-9 px-0 tip_fisica_bp' style='display: none'>
		<div class='col-md-4'>
			<div class='md-form'>
				<input type='text' id='bp_fisnombre' name='bp_fisnombre' class='form-control nombreF'>
				<label for='bp_fisnombre'>
					<liferay-ui:message key="CotizadorPaso1Portlet.lblNomFisicaP1" />
				</label>
			</div>
		</div>
		<div class='col-md-4'>
			<div class='md-form'>
				<input type='text' id='bp_fispaterno' name='bp_fispaterno' class='form-control apellidoP'>
				<label for='bp_fispaterno'>
					<liferay-ui:message key="CotizadorPaso1Portlet.lblApPatFisicaP1" />
				</label>
			</div>
		</div>
		<div class='col-md-4'>
			<div class='md-form'>
				<input type='text' id='bp_fismaterno' name='bp_fismaterno' class='form-control apellidoM'>
				<label for='bp_fismaterno'>
					<liferay-ui:message key="CotizadorPaso1Portlet.lblApMatFisicaP1" />
				</label>
			</div>
		</div>
	</div>
</div>
<div class='row infoPersona d-none'>
	<div class='col-md-12'>
		<input class="form-check-input float-right aplicaUbicaciones" type="checkbox" id="bp_aplicaUbicaciones" value="" />
    	<label for="bp_aplicaUbicaciones">
        	<span>
        		<liferay-ui:message key="CotizadorPaso2Portlet.lblaplicaUbicaciones" />
        	</span>
    	</label>
	</div>
</div>