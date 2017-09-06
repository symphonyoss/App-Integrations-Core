package org.symphonyoss.integration.pod.api.model;

/**
 * Holds all Key Manager data regarding an user.
 * Created by campidelli on 9/5/17.
 */
public class UserKeyManagerData {

  private String status;
  private Long userId;
  private String publicKey;
  private String privateKey;
  private String certificate;
  private String publicKeySignature;
  private String privateKeySignature;

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getPublicKey() {
    return publicKey;
  }

  public void setPublicKey(String publicKey) {
    this.publicKey = publicKey;
  }

  public String getPrivateKey() {
    return privateKey;
  }

  public void setPrivateKey(String privateKey) {
    this.privateKey = privateKey;
  }

  public String getCertificate() {
    return certificate;
  }

  public void setCertificate(String certificate) {
    this.certificate = certificate;
  }

  public String getPublicKeySignature() {
    return publicKeySignature;
  }

  public void setPublicKeySignature(String publicKeySignature) {
    this.publicKeySignature = publicKeySignature;
  }

  public String getPrivateKeySignature() {
    return privateKeySignature;
  }

  public void setPrivateKeySignature(String privateKeySignature) {
    this.privateKeySignature = privateKeySignature;
  }
}
