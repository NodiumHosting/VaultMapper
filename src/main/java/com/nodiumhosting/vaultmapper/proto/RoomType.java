// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: VaultMapperProtocol/vaultmapper.proto
// Protobuf Java Version: 4.28.3

package com.nodiumhosting.vaultmapper.proto;

/**
 * Protobuf enum {@code RoomType}
 */
public enum RoomType
    implements com.google.protobuf.ProtocolMessageEnum {
  /**
   * <code>ROOMTYPE_UNKNOWN = 0;</code>
   */
  ROOMTYPE_UNKNOWN(0),
  /**
   * <code>ROOMTYPE_START = 1;</code>
   */
  ROOMTYPE_START(1),
  /**
   * <code>ROOMTYPE_BASIC = 2;</code>
   */
  ROOMTYPE_BASIC(2),
  /**
   * <code>ROOMTYPE_ORE = 3;</code>
   */
  ROOMTYPE_ORE(3),
  /**
   * <code>ROOMTYPE_CHALLENGE = 4;</code>
   */
  ROOMTYPE_CHALLENGE(4),
  /**
   * <code>ROOMTYPE_OMEGA = 5;</code>
   */
  ROOMTYPE_OMEGA(5),
  UNRECOGNIZED(-1),
  ;

  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 28,
      /* patch= */ 3,
      /* suffix= */ "",
      RoomType.class.getName());
  }
  /**
   * <code>ROOMTYPE_UNKNOWN = 0;</code>
   */
  public static final int ROOMTYPE_UNKNOWN_VALUE = 0;
  /**
   * <code>ROOMTYPE_START = 1;</code>
   */
  public static final int ROOMTYPE_START_VALUE = 1;
  /**
   * <code>ROOMTYPE_BASIC = 2;</code>
   */
  public static final int ROOMTYPE_BASIC_VALUE = 2;
  /**
   * <code>ROOMTYPE_ORE = 3;</code>
   */
  public static final int ROOMTYPE_ORE_VALUE = 3;
  /**
   * <code>ROOMTYPE_CHALLENGE = 4;</code>
   */
  public static final int ROOMTYPE_CHALLENGE_VALUE = 4;
  /**
   * <code>ROOMTYPE_OMEGA = 5;</code>
   */
  public static final int ROOMTYPE_OMEGA_VALUE = 5;


  public final int getNumber() {
    if (this == UNRECOGNIZED) {
      throw new java.lang.IllegalArgumentException(
          "Can't get the number of an unknown enum value.");
    }
    return value;
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   * @deprecated Use {@link #forNumber(int)} instead.
   */
  @java.lang.Deprecated
  public static RoomType valueOf(int value) {
    return forNumber(value);
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   */
  public static RoomType forNumber(int value) {
    switch (value) {
      case 0: return ROOMTYPE_UNKNOWN;
      case 1: return ROOMTYPE_START;
      case 2: return ROOMTYPE_BASIC;
      case 3: return ROOMTYPE_ORE;
      case 4: return ROOMTYPE_CHALLENGE;
      case 5: return ROOMTYPE_OMEGA;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<RoomType>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      RoomType> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<RoomType>() {
          public RoomType findValueByNumber(int number) {
            return RoomType.forNumber(number);
          }
        };

  public final com.google.protobuf.Descriptors.EnumValueDescriptor
      getValueDescriptor() {
    if (this == UNRECOGNIZED) {
      throw new java.lang.IllegalStateException(
          "Can't get the descriptor of an unrecognized enum value.");
    }
    return getDescriptor().getValues().get(ordinal());
  }
  public final com.google.protobuf.Descriptors.EnumDescriptor
      getDescriptorForType() {
    return getDescriptor();
  }
  public static final com.google.protobuf.Descriptors.EnumDescriptor
      getDescriptor() {
    return com.nodiumhosting.vaultmapper.proto.VaultMapperProto.getDescriptor().getEnumTypes().get(2);
  }

  private static final RoomType[] VALUES = values();

  public static RoomType valueOf(
      com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
    if (desc.getType() != getDescriptor()) {
      throw new java.lang.IllegalArgumentException(
        "EnumValueDescriptor is not for this type.");
    }
    if (desc.getIndex() == -1) {
      return UNRECOGNIZED;
    }
    return VALUES[desc.getIndex()];
  }

  private final int value;

  private RoomType(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:RoomType)
}

