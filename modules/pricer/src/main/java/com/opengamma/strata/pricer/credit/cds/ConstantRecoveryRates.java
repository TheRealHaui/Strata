/**
 * Copyright (C) 2016 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.pricer.credit.cds;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.ImmutableValidator;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.strata.basics.StandardId;
import com.opengamma.strata.collect.ArgChecker;

/**
 * The constant recovery rate.
 * <p>
 * The recovery rate is constant for any given date. Thus {@code CurrencyParameterSensitivities} is not computed in 
 * this implementation.
 */
@BeanDefinition(builderScope = "private")
public final class ConstantRecoveryRates
    implements RecoveryRates, ImmutableBean, Serializable {

  @PropertyDefinition(validate = "notNull")
  private final StandardId legalEntityId;
  /**
   * The valuation date.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private final LocalDate valuationDate;
  /**
   * The underlying curve.
   * The metadata of the curve must define a day count.
   */
  @PropertyDefinition
  private final double recoveryRate;

  //-------------------------------------------------------------------------
  /**
   * Obtains an instance.
   * 
   * @param legalEntityId  the legal entity identifier
   * @param valuationDate  the valuation date
   * @param recoveryRate  the recovery rate
   * @return the instance
   */
  public static ConstantRecoveryRates of(StandardId legalEntityId, LocalDate valuationDate, double recoveryRate) {
    return new ConstantRecoveryRates(legalEntityId, valuationDate, recoveryRate);
  }

  @ImmutableValidator
  private void validate() {
    ArgChecker.inRangeInclusive(recoveryRate, 0d, 1d, "recovery rate must be between 0 and 1");
  }

  //-------------------------------------------------------------------------
  @Override
  public double recoveryRate(LocalDate date) {
    return recoveryRate;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ConstantRecoveryRates}.
   * @return the meta-bean, not null
   */
  public static ConstantRecoveryRates.Meta meta() {
    return ConstantRecoveryRates.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ConstantRecoveryRates.Meta.INSTANCE);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  private ConstantRecoveryRates(
      StandardId legalEntityId,
      LocalDate valuationDate,
      double recoveryRate) {
    JodaBeanUtils.notNull(legalEntityId, "legalEntityId");
    JodaBeanUtils.notNull(valuationDate, "valuationDate");
    this.legalEntityId = legalEntityId;
    this.valuationDate = valuationDate;
    this.recoveryRate = recoveryRate;
    validate();
  }

  @Override
  public ConstantRecoveryRates.Meta metaBean() {
    return ConstantRecoveryRates.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the legalEntityId.
   * @return the value of the property, not null
   */
  public StandardId getLegalEntityId() {
    return legalEntityId;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the valuation date.
   * @return the value of the property, not null
   */
  @Override
  public LocalDate getValuationDate() {
    return valuationDate;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the underlying curve.
   * The metadata of the curve must define a day count.
   * @return the value of the property
   */
  public double getRecoveryRate() {
    return recoveryRate;
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ConstantRecoveryRates other = (ConstantRecoveryRates) obj;
      return JodaBeanUtils.equal(legalEntityId, other.legalEntityId) &&
          JodaBeanUtils.equal(valuationDate, other.valuationDate) &&
          JodaBeanUtils.equal(recoveryRate, other.recoveryRate);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(legalEntityId);
    hash = hash * 31 + JodaBeanUtils.hashCode(valuationDate);
    hash = hash * 31 + JodaBeanUtils.hashCode(recoveryRate);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("ConstantRecoveryRates{");
    buf.append("legalEntityId").append('=').append(legalEntityId).append(',').append(' ');
    buf.append("valuationDate").append('=').append(valuationDate).append(',').append(' ');
    buf.append("recoveryRate").append('=').append(JodaBeanUtils.toString(recoveryRate));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ConstantRecoveryRates}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code legalEntityId} property.
     */
    private final MetaProperty<StandardId> legalEntityId = DirectMetaProperty.ofImmutable(
        this, "legalEntityId", ConstantRecoveryRates.class, StandardId.class);
    /**
     * The meta-property for the {@code valuationDate} property.
     */
    private final MetaProperty<LocalDate> valuationDate = DirectMetaProperty.ofImmutable(
        this, "valuationDate", ConstantRecoveryRates.class, LocalDate.class);
    /**
     * The meta-property for the {@code recoveryRate} property.
     */
    private final MetaProperty<Double> recoveryRate = DirectMetaProperty.ofImmutable(
        this, "recoveryRate", ConstantRecoveryRates.class, Double.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "legalEntityId",
        "valuationDate",
        "recoveryRate");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 866287159:  // legalEntityId
          return legalEntityId;
        case 113107279:  // valuationDate
          return valuationDate;
        case 2002873877:  // recoveryRate
          return recoveryRate;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ConstantRecoveryRates> builder() {
      return new ConstantRecoveryRates.Builder();
    }

    @Override
    public Class<? extends ConstantRecoveryRates> beanType() {
      return ConstantRecoveryRates.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code legalEntityId} property.
     * @return the meta-property, not null
     */
    public MetaProperty<StandardId> legalEntityId() {
      return legalEntityId;
    }

    /**
     * The meta-property for the {@code valuationDate} property.
     * @return the meta-property, not null
     */
    public MetaProperty<LocalDate> valuationDate() {
      return valuationDate;
    }

    /**
     * The meta-property for the {@code recoveryRate} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Double> recoveryRate() {
      return recoveryRate;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 866287159:  // legalEntityId
          return ((ConstantRecoveryRates) bean).getLegalEntityId();
        case 113107279:  // valuationDate
          return ((ConstantRecoveryRates) bean).getValuationDate();
        case 2002873877:  // recoveryRate
          return ((ConstantRecoveryRates) bean).getRecoveryRate();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code ConstantRecoveryRates}.
   */
  private static final class Builder extends DirectFieldsBeanBuilder<ConstantRecoveryRates> {

    private StandardId legalEntityId;
    private LocalDate valuationDate;
    private double recoveryRate;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 866287159:  // legalEntityId
          return legalEntityId;
        case 113107279:  // valuationDate
          return valuationDate;
        case 2002873877:  // recoveryRate
          return recoveryRate;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 866287159:  // legalEntityId
          this.legalEntityId = (StandardId) newValue;
          break;
        case 113107279:  // valuationDate
          this.valuationDate = (LocalDate) newValue;
          break;
        case 2002873877:  // recoveryRate
          this.recoveryRate = (Double) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Builder setString(MetaProperty<?> property, String value) {
      super.setString(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public ConstantRecoveryRates build() {
      return new ConstantRecoveryRates(
          legalEntityId,
          valuationDate,
          recoveryRate);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(128);
      buf.append("ConstantRecoveryRates.Builder{");
      buf.append("legalEntityId").append('=').append(JodaBeanUtils.toString(legalEntityId)).append(',').append(' ');
      buf.append("valuationDate").append('=').append(JodaBeanUtils.toString(valuationDate)).append(',').append(' ');
      buf.append("recoveryRate").append('=').append(JodaBeanUtils.toString(recoveryRate));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
