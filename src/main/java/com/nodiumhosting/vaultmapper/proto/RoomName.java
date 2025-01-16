// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: VaultMapperProtocol/vaultmapper.proto
// Protobuf Java Version: 4.28.3

package com.nodiumhosting.vaultmapper.proto;

/**
 * Protobuf enum {@code RoomName}
 */
public enum RoomName
    implements com.google.protobuf.ProtocolMessageEnum {
  /**
   * <code>ROOMNAME_UNKNOWN = 0;</code>
   */
  ROOMNAME_UNKNOWN(0),
  /**
   * <code>ROOMNAME_BLACKSMITH = 1;</code>
   */
  ROOMNAME_BLACKSMITH(1),
  /**
   * <code>ROOMNAME_COVE = 2;</code>
   */
  ROOMNAME_COVE(2),
  /**
   * <code>ROOMNAME_CRYSTAL_CAVES = 3;</code>
   */
  ROOMNAME_CRYSTAL_CAVES(3),
  /**
   * <code>ROOMNAME_DIG_SITE = 4;</code>
   */
  ROOMNAME_DIG_SITE(4),
  /**
   * <code>ROOMNAME_DRAGON = 5;</code>
   */
  ROOMNAME_DRAGON(5),
  /**
   * <code>ROOMNAME_FACTORY = 6;</code>
   */
  ROOMNAME_FACTORY(6),
  /**
   * <code>ROOMNAME_LIBRARY = 7;</code>
   */
  ROOMNAME_LIBRARY(7),
  /**
   * <code>ROOMNAME_MINE = 8;</code>
   */
  ROOMNAME_MINE(8),
  /**
   * <code>ROOMNAME_MUSH_ROOM = 9;</code>
   */
  ROOMNAME_MUSH_ROOM(9),
  /**
   * <code>ROOMNAME_PAINTING = 10;</code>
   */
  ROOMNAME_PAINTING(10),
  /**
   * <code>ROOMNAME_VENDOR = 11;</code>
   */
  ROOMNAME_VENDOR(11),
  /**
   * <code>ROOMNAME_VILLAGE = 12;</code>
   */
  ROOMNAME_VILLAGE(12),
  /**
   * <code>ROOMNAME_WILD_WEST = 13;</code>
   */
  ROOMNAME_WILD_WEST(13),
  /**
   * <code>ROOMNAME_X_MARK = 14;</code>
   */
  ROOMNAME_X_MARK(14),
  /**
   * <code>ROOMNAME_CUBE = 15;</code>
   */
  ROOMNAME_CUBE(15),
  /**
   * <code>ROOMNAME_LAB = 16;</code>
   */
  ROOMNAME_LAB(16),
  /**
   * <code>ROOMNAME_RAID = 17;</code>
   */
  ROOMNAME_RAID(17),
  UNRECOGNIZED(-1),
  ;

  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 28,
      /* patch= */ 3,
      /* suffix= */ "",
      RoomName.class.getName());
  }
  /**
   * <code>ROOMNAME_UNKNOWN = 0;</code>
   */
  public static final int ROOMNAME_UNKNOWN_VALUE = 0;
  /**
   * <code>ROOMNAME_BLACKSMITH = 1;</code>
   */
  public static final int ROOMNAME_BLACKSMITH_VALUE = 1;
  /**
   * <code>ROOMNAME_COVE = 2;</code>
   */
  public static final int ROOMNAME_COVE_VALUE = 2;
  /**
   * <code>ROOMNAME_CRYSTAL_CAVES = 3;</code>
   */
  public static final int ROOMNAME_CRYSTAL_CAVES_VALUE = 3;
  /**
   * <code>ROOMNAME_DIG_SITE = 4;</code>
   */
  public static final int ROOMNAME_DIG_SITE_VALUE = 4;
  /**
   * <code>ROOMNAME_DRAGON = 5;</code>
   */
  public static final int ROOMNAME_DRAGON_VALUE = 5;
  /**
   * <code>ROOMNAME_FACTORY = 6;</code>
   */
  public static final int ROOMNAME_FACTORY_VALUE = 6;
  /**
   * <code>ROOMNAME_LIBRARY = 7;</code>
   */
  public static final int ROOMNAME_LIBRARY_VALUE = 7;
  /**
   * <code>ROOMNAME_MINE = 8;</code>
   */
  public static final int ROOMNAME_MINE_VALUE = 8;
  /**
   * <code>ROOMNAME_MUSH_ROOM = 9;</code>
   */
  public static final int ROOMNAME_MUSH_ROOM_VALUE = 9;
  /**
   * <code>ROOMNAME_PAINTING = 10;</code>
   */
  public static final int ROOMNAME_PAINTING_VALUE = 10;
  /**
   * <code>ROOMNAME_VENDOR = 11;</code>
   */
  public static final int ROOMNAME_VENDOR_VALUE = 11;
  /**
   * <code>ROOMNAME_VILLAGE = 12;</code>
   */
  public static final int ROOMNAME_VILLAGE_VALUE = 12;
  /**
   * <code>ROOMNAME_WILD_WEST = 13;</code>
   */
  public static final int ROOMNAME_WILD_WEST_VALUE = 13;
  /**
   * <code>ROOMNAME_X_MARK = 14;</code>
   */
  public static final int ROOMNAME_X_MARK_VALUE = 14;
  /**
   * <code>ROOMNAME_CUBE = 15;</code>
   */
  public static final int ROOMNAME_CUBE_VALUE = 15;
  /**
   * <code>ROOMNAME_LAB = 16;</code>
   */
  public static final int ROOMNAME_LAB_VALUE = 16;
  /**
   * <code>ROOMNAME_RAID = 17;</code>
   */
  public static final int ROOMNAME_RAID_VALUE = 17;


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
  public static RoomName valueOf(int value) {
    return forNumber(value);
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   */
  public static RoomName forNumber(int value) {
    switch (value) {
      case 0: return ROOMNAME_UNKNOWN;
      case 1: return ROOMNAME_BLACKSMITH;
      case 2: return ROOMNAME_COVE;
      case 3: return ROOMNAME_CRYSTAL_CAVES;
      case 4: return ROOMNAME_DIG_SITE;
      case 5: return ROOMNAME_DRAGON;
      case 6: return ROOMNAME_FACTORY;
      case 7: return ROOMNAME_LIBRARY;
      case 8: return ROOMNAME_MINE;
      case 9: return ROOMNAME_MUSH_ROOM;
      case 10: return ROOMNAME_PAINTING;
      case 11: return ROOMNAME_VENDOR;
      case 12: return ROOMNAME_VILLAGE;
      case 13: return ROOMNAME_WILD_WEST;
      case 14: return ROOMNAME_X_MARK;
      case 15: return ROOMNAME_CUBE;
      case 16: return ROOMNAME_LAB;
      case 17: return ROOMNAME_RAID;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<RoomName>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      RoomName> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<RoomName>() {
          public RoomName findValueByNumber(int number) {
            return RoomName.forNumber(number);
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
    return com.nodiumhosting.vaultmapper.proto.VaultMapperProto.getDescriptor().getEnumTypes().get(3);
  }

  private static final RoomName[] VALUES = values();

  public static RoomName valueOf(
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

  private RoomName(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:RoomName)
}

