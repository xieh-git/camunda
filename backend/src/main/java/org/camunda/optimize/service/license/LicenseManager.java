package org.camunda.optimize.service.license;

import org.camunda.bpm.licensecheck.InvalidLicenseException;
import org.camunda.bpm.licensecheck.LicenseKey;
import org.camunda.bpm.licensecheck.OptimizeLicenseKey;
import org.camunda.optimize.dto.optimize.query.LicenseInformationDto;
import org.camunda.optimize.service.exceptions.OptimizeException;
import org.camunda.optimize.service.util.configuration.ConfigurationService;
import org.camunda.optimize.upgrade.es.ElasticsearchConstants;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static org.camunda.optimize.service.es.schema.OptimizeIndexNameHelper.getOptimizeIndexAliasForType;
import static org.camunda.optimize.service.es.schema.type.LicenseType.LICENSE;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

@Component
public class LicenseManager {

  @Autowired
  private ConfigurationService configurationService;
  @Autowired
  private Client esclient;

  private final String licenseDocumentId = "license";
  private LicenseKey licenseKey = new OptimizeLicenseKey();
  private String optimizeLicense;
  
  @PostConstruct
  public void init() {
    optimizeLicense = retrieveStoredOptimizeLicense();
    if (optimizeLicense == null) {
      try {
        optimizeLicense = readFileToString("OptimizeLicense.txt");
        storeLicense(optimizeLicense);
      } catch (Exception ignored) {
        // nothing to do here
      }
    }
  }

  private String readFileToString(String filePath) throws IOException, URISyntaxException {
    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath);
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int length;
    while ((length = inputStream.read(buffer)) != -1) {
      result.write(buffer, 0, length);
    }
    return result.toString(StandardCharsets.UTF_8.name());
  }

  public void storeLicense(String licenseAsString) throws OptimizeException {
    XContentBuilder builder;
    try {
      builder = jsonBuilder()
        .startObject()
          .field(LICENSE, licenseAsString)
        .endObject();
    } catch (IOException exception) {
      throw new OptimizeException("Could not parse given license. Please check the encoding!");
    }

    IndexResponse response = esclient
      .prepareIndex(
        getOptimizeIndexAliasForType(ElasticsearchConstants.LICENSE_TYPE),
        ElasticsearchConstants.LICENSE_TYPE,
        licenseDocumentId
      )
      .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
      .setSource(builder)
      .get();

    boolean licenseWasStored = response.getId() != null;
    if (!licenseWasStored) {
      throw new OptimizeException("Could not store license in Elasticsearch. Please check the connection!");
    } else {
      optimizeLicense = licenseAsString;
    }
  }

  private String retrieveLicense() throws InvalidLicenseException {
    if (optimizeLicense == null) {
      throw new InvalidLicenseException("No license stored in Optimize. Please provide a valid Optimize license");
    }
    return optimizeLicense;
  }

  private String retrieveStoredOptimizeLicense() {
    GetResponse response = esclient
      .prepareGet(
        getOptimizeIndexAliasForType(ElasticsearchConstants.LICENSE_TYPE),
        ElasticsearchConstants.LICENSE_TYPE,
        licenseDocumentId
      )
      .get();

    String licenseAsString = null;
    if (response.isExists()) {
      licenseAsString = response.getSource().get(LICENSE).toString();
    }
    return licenseAsString;
  }

  public LicenseInformationDto validateOptimizeLicense(String licenseAsString) throws InvalidLicenseException {
    if (licenseAsString == null) {
      throw new InvalidLicenseException("Could not validate given license. Please try to provide another license!");
    }
    licenseKey.createLicenseKey(licenseAsString);
    licenseKey.validate();
    return licenseKeyToDto(licenseKey);
  }

  private LicenseInformationDto licenseKeyToDto(LicenseKey licenseKey) {
    LicenseInformationDto dto = new LicenseInformationDto();
    dto.setCustomerId(licenseKey.getCustomerId());
    dto.setUnlimited(licenseKey.isUnlimited());
    if(!licenseKey.isUnlimited()) {
      dto.setValidUntil(OffsetDateTime.ofInstant(licenseKey.getValidUntil().toInstant(), ZoneId.systemDefault()));
    }
    return dto;
  }

  public LicenseInformationDto validateLicenseStoredInOptimize() throws InvalidLicenseException {
    String license = retrieveLicense();
    return validateOptimizeLicense(license);
  }

  public void setOptimizeLicense(String optimizeLicense) {
    this.optimizeLicense = optimizeLicense;
  }

  public void resetLicenseFromFile() {
    this.optimizeLicense = null;
  }

}
