// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: VaultMapperProtocol/vaultmapper.proto
// Protobuf Java Version: 4.30.1

package com.nodiumhosting.vaultmapper.proto;

/**
 * Protobuf enum {@code CellType}
 */
public enum CellType
    implements com.google.protobuf.ProtocolMessageEnum {
  /**
   * <code>CELLTYPE_UNKNOWN = 0;</code>
   */
  CELLTYPE_UNKNOWN(0),
  /**
   * <code>CELLTYPE_ROOM = 1;</code>
   */
  CELLTYPE_ROOM(1),
  /**
   * <code>CELLTYPE_TUNNEL_X = 2;</code>
   */
  CELLTYPE_TUNNEL_X(2),
  /**
   * <code>CELLTYPE_TUNNEL_Z = 3;</code>
   */
  CELLTYPE_TUNNEL_Z(3),
  UNRECOGNIZED(-1),
  ;

  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 30,
      /* patch= */ 1,
      /* suffix= */ "",
      CellType.class.getName());
  }
  /**
   * <code>CELLTYPE_UNKNOWN = 0;</code>
   */
  public static final int CELLTYPE_UNKNOWN_VALUE = 0;
  /**
   * <code>CELLTYPE_ROOM = 1;</code>
   */
  public static final int CELLTYPE_ROOM_VALUE = 1;
  /**
   * <code>CELLTYPE_TUNNEL_X = 2;</code>
   */
  public static final int CELLTYPE_TUNNEL_X_VALUE = 2;
  /**
   * <code>CELLTYPE_TUNNEL_Z = 3;</code>
   */
  public static final int CELLTYPE_TUNNEL_Z_VALUE = 3;


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
  public static CellType valueOf(int value) {
    return forNumber(value);
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   */
  public static CellType forNumber(int value) {
    switch (value) {
      case 0: return CELLTYPE_UNKNOWN;
      case 1: return CELLTYPE_ROOM;
      case 2: return CELLTYPE_TUNNEL_X;
      case 3: return CELLTYPE_TUNNEL_Z;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<CellType>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      CellType> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<CellType>() {
          public CellType findValueByNumber(int number) {
            return CellType.forNumber(number);
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
    return com.nodiumhosting.vaultmapper.proto.VaultMapperProto.getDescriptor().getEnumTypes().get(1);
  }

  private static final CellType[] VALUES = values();

  public static CellType valueOf(
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

  private CellType(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:CellType)
}

