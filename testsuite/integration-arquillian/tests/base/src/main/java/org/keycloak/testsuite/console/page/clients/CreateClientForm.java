package org.keycloak.testsuite.console.page.clients;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jboss.arquillian.graphene.page.Page;
import org.keycloak.representations.idm.ClientRepresentation;
import static org.keycloak.testsuite.auth.page.login.OIDCLogin.OIDC;
import org.keycloak.testsuite.console.page.fragment.OnOffSwitch;
import org.keycloak.testsuite.page.Form;
import static org.keycloak.testsuite.page.Form.getInputValue;
import static org.keycloak.testsuite.util.WaitUtils.*;
import org.keycloak.testsuite.util.Timer;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

/**
 *
 * @author tkyjovsk
 */
public class CreateClientForm extends Form {

    @FindBy(id = "clientId")
    private WebElement clientIdInput;

    @FindBy(id = "name")
    private WebElement nameInput;

    @FindBy(xpath = ".//div[@class='onoffswitch' and ./input[@id='enabled']]")
    private OnOffSwitch enabledSwitch;

    @FindBy(xpath = ".//div[@class='onoffswitch' and ./input[@id='consentRequired']]")
    private OnOffSwitch consentRequiredSwitch;

    @FindBy(xpath = ".//div[@class='onoffswitch' and ./input[@id='directGrantsOnly']]")
    private OnOffSwitch directGrantsOnlySwitch;

    @FindBy(id = "protocol")
    private Select protocolSelect;
    
    @Page
    private SAMLClientSettingsForm samlForm;

    public SAMLClientSettingsForm samlForm() {
        return samlForm;
    }

    @FindBy(id = "accessType")
    private Select accessTypeSelect;
    @FindBy(xpath = ".//div[@class='onoffswitch' and ./input[@id='serviceAccountsEnabled']]")
    private OnOffSwitch serviceAccountsEnabledSwitch;

    @FindBy(id = "newRedirectUri")
    private WebElement newRedirectUriInput;
    @FindBy(xpath = ".//i[contains(@data-ng-click, 'newRedirectUri')]")
    private WebElement newRedirectUriSubmit;
    @FindBy(xpath = ".//input[@ng-model='client.redirectUris[i]']")
    private List<WebElement> redirectUriInputs;
    @FindBy(xpath = ".//i[contains(@data-ng-click, 'deleteRedirectUri')]")
    private List<WebElement> deleteRedirectUriIcons;

    public void setValues(ClientRepresentation client) {
        waitAjaxForElement(clientIdInput);

        setClientId(client.getClientId());
        setName(client.getName());
        setEnabled(client.isEnabled());
        setConsentRequired(client.isConsentRequired());
        setDirectGrantsOnly(client.isDirectGrantsOnly());
        setProtocol(client.getProtocol());
        if (OIDC.equals(client.getProtocol())) {
            setAccessType(client);
            if (!client.isBearerOnly()) {
                if (!client.isPublicClient()) {
                    setServiceAccountsEnabled(client.isServiceAccountsEnabled());
                }
                setRedirectUris(client.getRedirectUris());
            }
        }
    }

    public ClientRepresentation getValues() {
        ClientRepresentation values = new ClientRepresentation();
        values.setClientId(getClientId());
        values.setName(getName());
        values.setEnabled(isEnabled());
        values.setConsentRequired(isConsentRequired());
        values.setDirectGrantsOnly(isDirectGrantsOnly());
        values.setProtocol(getProtocol());
        if (OIDC.equals(values.getProtocol())) {
            values.setBearerOnly(isBearerOnly());
            if (!values.isBearerOnly()) {
                values.setPublicClient(isPublicClient());
                if (!values.isPublicClient()) {
                    values.setServiceAccountsEnabled(isServiceAccountsEnabled());
                }
                values.setRedirectUris(getRedirectUris());
            }
        }
        return values;
    }

    public String getClientId() {
        return getInputValue(clientIdInput);
    }

    public void setClientId(String clientId) {
        setInputValue(clientIdInput, clientId);
    }

    public String getName() {
        return getInputValue(nameInput);
    }

    public void setName(String name) {
        setInputValue(nameInput, name);
    }

    public boolean isEnabled() {
        return enabledSwitch.isOn();
    }

    public void setEnabled(boolean enabled) {
        enabledSwitch.setOn(enabled);
    }

    public static final String BEARER_ONLY = "bearer-only";
    public static final String PUBLIC = "public";
    public static final String CONFIDENTIAL = "confidential";
        
    public boolean isBearerOnly() {
        return BEARER_ONLY.equals(
                accessTypeSelect.getFirstSelectedOption().getAttribute(VALUE));
    }

    public boolean isPublicClient() {
        return PUBLIC.equals(
                accessTypeSelect.getFirstSelectedOption().getAttribute(VALUE));
    }

    public void setAccessType(ClientRepresentation client) {
        if (client.isBearerOnly()) {
            accessTypeSelect.selectByVisibleText(BEARER_ONLY);
        } else if (client.isPublicClient()) {
            accessTypeSelect.selectByVisibleText(PUBLIC);
        } else {
            accessTypeSelect.selectByVisibleText(CONFIDENTIAL);
        }
    }

    public void addRedirectUri(String redirectUri) {
        newRedirectUriInput.sendKeys(redirectUri);
        newRedirectUriSubmit.click();
    }

    public List<String> getRedirectUris() {
        List<String> values = new ArrayList<>();
        for (WebElement input : redirectUriInputs) {
            values.add(getInputValue(input));
        }
        return values;
    }

    public void setRedirectUris(List<String> redirectUris) {
        Timer.time();
        while (!deleteRedirectUriIcons.isEmpty()) {
            deleteRedirectUriIcons.get(0).click();
            pause(100);
        }
        Timer.time("deleteRedirectUris");
        if (redirectUris != null) {
            for (String redirectUri : redirectUris) {
                addRedirectUri(redirectUri);
                pause(100);
            }
        }
        Timer.time("addRedirectUris");
    }

    public boolean isConsentRequired() {
        return consentRequiredSwitch.isOn();
    }

    public void setConsentRequired(boolean consentRequired) {
        consentRequiredSwitch.setOn(consentRequired);
    }

    public boolean isDirectGrantsOnly() {
        return directGrantsOnlySwitch.isOn();
    }

    public void setDirectGrantsOnly(boolean directGrantsOnly) {
        directGrantsOnlySwitch.setOn(directGrantsOnly);
    }

    public String getProtocol() {
        waitAjaxForElement(protocolSelect.getFirstSelectedOption());
        return protocolSelect.getFirstSelectedOption().getText();
    }

    public void setProtocol(String protocol) {
        Timer.time();
        protocolSelect.selectByVisibleText(protocol);
        Timer.time("clientSettings.setProtocol()");
    }

    public boolean isServiceAccountsEnabled() {
        return serviceAccountsEnabledSwitch.isOn();
    }

    public void setServiceAccountsEnabled(boolean serviceAccountsEnabled) {
        serviceAccountsEnabledSwitch.setOn(serviceAccountsEnabled);
    }

    public class SAMLClientSettingsForm extends Form {

        public static final String SAML_ASSERTION_SIGNATURE = "saml.assertion.signature";
        public static final String SAML_AUTHNSTATEMENT = "saml.authnstatement";
	public static final String SAML_CLIENT_SIGNATURE = "saml.client.signature";
	public static final String SAML_ENCRYPT = "saml.encrypt";
	public static final String SAML_FORCE_POST_BINDING = "saml.force.post.binding";
	public static final String SAML_MULTIVALUED_ROLES = "saml.multivalued.roles";
	public static final String SAML_SERVER_SIGNATURE = "saml.server.signature";
	public static final String SAML_SIGNATURE_ALGORITHM = "saml.signature.algorithm";
	public static final String SAML_ASSERTION_CONSUMER_URL_POST = "saml_assertion_consumer_url_post";
	public static final String SAML_ASSERTION_CONSUMER_URL_REDIRECT = "saml_assertion_consumer_url_redirect";
	public static final String SAML_FORCE_NAME_ID_FORMAT = "saml_force_name_id_format";
	public static final String SAML_NAME_ID_FORMAT = "saml_name_id_format";
	public static final String SAML_SIGNATURE_CANONICALIZATION_METHOD = "saml_signature_canonicalization_method";
	public static final String SAML_SINGLE_LOGOUT_SERVICE_URL_POST = "saml_single_logout_service_url_post";
	public static final String SAML_SINGLE_LOGOUT_SERVICE_URL_REDIRECT = "saml_single_logout_service_url_redirect";
        
        @FindBy(xpath = ".//div[@class='onoffswitch' and ./input[@id='samlAuthnStatement']]")
        private OnOffSwitch samlAuthnStatement;
        @FindBy(xpath = ".//div[@class='onoffswitch' and ./input[@id='samlServerSignature']]")
        private OnOffSwitch samlServerSignature;
        @FindBy(xpath = ".//div[@class='onoffswitch' and ./input[@id='samlAssertionSignature']]")
        private OnOffSwitch samlAssertionSignature;
        @FindBy(id = "signatureAlgorithm")
        private Select signatureAlgorithm;
        @FindBy(id = "canonicalization")
        private Select canonicalization;
        @FindBy(xpath = ".//div[@class='onoffswitch' and ./input[@id='samlEncrypt']]")
        private OnOffSwitch samlEncrypt;
        @FindBy(xpath = ".//div[@class='onoffswitch' and ./input[@id='samlClientSignature']]")
        private OnOffSwitch samlClientSignature;
        @FindBy(xpath = ".//div[@class='onoffswitch' and ./input[@id='samlForcePostBinding']]")
        private OnOffSwitch samlForcePostBinding;
        @FindBy(xpath = ".//div[@class='onoffswitch' and ./input[@id='frontchannelLogout']]")
        private OnOffSwitch frontchannelLogout;
        @FindBy(xpath = ".//div[@class='onoffswitch' and ./input[@id='samlForceNameIdFormat']]")
        private OnOffSwitch samlForceNameIdFormat;
        @FindBy(id = "samlNameIdFormat")
        private Select samlNameIdFormat;
        
        @FindBy(xpath = "//fieldset[contains(@data-ng-show, 'saml')]//i")
        private WebElement fineGrainCollapsor;
        
        @FindBy(id = "consumerServicePost")
        private WebElement consumerServicePostInput;
        @FindBy(id = "consumerServiceRedirect")
        private WebElement consumerServiceRedirectInput;
        @FindBy(id = "logoutPostBinding")
        private WebElement logoutPostBindingInput;
        @FindBy(id = "logoutRedirectBinding")
        private WebElement logoutRedirectBindingInput;
        
        public void setValues(ClientRepresentation client) {
            waitAjaxForElementVisible(fineGrainCollapsor);
            
            Map<String, String> attributes = client.getAttributes();
            samlAuthnStatement.setOn("true".equals(attributes.get(SAML_AUTHNSTATEMENT)));
            samlServerSignature.setOn("true".equals(attributes.get(SAML_SERVER_SIGNATURE)));
            samlAssertionSignature.setOn("true".equals(attributes.get(SAML_ASSERTION_SIGNATURE)));
            if (samlServerSignature.isOn() || samlAssertionSignature.isOn()) {
                signatureAlgorithm.selectByVisibleText(attributes.get(SAML_SIGNATURE_ALGORITHM));
                canonicalization.selectByValue("string:" + attributes.get(SAML_SIGNATURE_CANONICALIZATION_METHOD));
            }
            samlEncrypt.setOn("true".equals(attributes.get(SAML_ENCRYPT)));
            samlClientSignature.setOn("true".equals(attributes.get(SAML_CLIENT_SIGNATURE)));
            samlForcePostBinding.setOn("true".equals(attributes.get(SAML_FORCE_POST_BINDING)));
            frontchannelLogout.setOn(client.isFrontchannelLogout());
            samlForceNameIdFormat.setOn("true".equals(attributes.get(SAML_FORCE_NAME_ID_FORMAT)));
            samlNameIdFormat.selectByVisibleText(attributes.get(SAML_NAME_ID_FORMAT));
            
            fineGrainCollapsor.click();
            waitAjaxForElement(consumerServicePostInput);
            
            setInputValue(consumerServicePostInput, attributes.get(SAML_ASSERTION_CONSUMER_URL_POST));
            setInputValue(consumerServiceRedirectInput, attributes.get(SAML_ASSERTION_CONSUMER_URL_REDIRECT));
            setInputValue(logoutPostBindingInput, attributes.get(SAML_SINGLE_LOGOUT_SERVICE_URL_POST));
            setInputValue(logoutRedirectBindingInput, attributes.get(SAML_SINGLE_LOGOUT_SERVICE_URL_REDIRECT));
        }
    }

}
