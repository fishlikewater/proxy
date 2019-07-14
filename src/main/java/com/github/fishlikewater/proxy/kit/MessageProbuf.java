package com.github.fishlikewater.proxy.kit;// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: message.proto

public final class MessageProbuf {
  private MessageProbuf() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  /**
   * Protobuf enum {@code MessageType}
   */
  public enum MessageType
      implements com.google.protobuf.ProtocolMessageEnum {
    /**
     * <code>CONNECTION = 0;</code>
     */
    CONNECTION(0),
    /**
     * <code>HEALTH = 1;</code>
     */
    HEALTH(1),
    /**
     * <code>CLOSE = 2;</code>
     */
    CLOSE(2),
    /**
     * <code>VALID = 3;</code>
     */
    VALID(3),
    /**
     * <code>CHECK = 4;</code>
     */
    CHECK(4),
    /**
     * <code>RESULT = 5;</code>
     */
    RESULT(5),
    /**
     * <code>REFRESH = 6;</code>
     */
    REFRESH(6),
    /**
     * <code>LOGOUT = 7;</code>
     */
    LOGOUT(7),
    /**
     * <code>LOGIN = 8;</code>
     */
    LOGIN(8),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>CONNECTION = 0;</code>
     */
    public static final int CONNECTION_VALUE = 0;
    /**
     * <code>HEALTH = 1;</code>
     */
    public static final int HEALTH_VALUE = 1;
    /**
     * <code>CLOSE = 2;</code>
     */
    public static final int CLOSE_VALUE = 2;
    /**
     * <code>VALID = 3;</code>
     */
    public static final int VALID_VALUE = 3;
    /**
     * <code>CHECK = 4;</code>
     */
    public static final int CHECK_VALUE = 4;
    /**
     * <code>RESULT = 5;</code>
     */
    public static final int RESULT_VALUE = 5;
    /**
     * <code>REFRESH = 6;</code>
     */
    public static final int REFRESH_VALUE = 6;
    /**
     * <code>LOGOUT = 7;</code>
     */
    public static final int LOGOUT_VALUE = 7;
    /**
     * <code>LOGIN = 8;</code>
     */
    public static final int LOGIN_VALUE = 8;


    public final int getNumber() {
      if (this == UNRECOGNIZED) {
        throw new IllegalArgumentException(
            "Can't get the number of an unknown enum value.");
      }
      return value;
    }

    /**
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @Deprecated
    public static MessageType valueOf(int value) {
      return forNumber(value);
    }

    public static MessageType forNumber(int value) {
      switch (value) {
        case 0: return CONNECTION;
        case 1: return HEALTH;
        case 2: return CLOSE;
        case 3: return VALID;
        case 4: return CHECK;
        case 5: return RESULT;
        case 6: return REFRESH;
        case 7: return LOGOUT;
        case 8: return LOGIN;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<MessageType>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
        MessageType> internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<MessageType>() {
            public MessageType findValueByNumber(int number) {
              return MessageType.forNumber(number);
            }
          };

    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      return getDescriptor().getValues().get(ordinal());
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return MessageProbuf.getDescriptor().getEnumTypes().get(0);
    }

    private static final MessageType[] VALUES = values();

    public static MessageType valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      if (desc.getIndex() == -1) {
        return UNRECOGNIZED;
      }
      return VALUES[desc.getIndex()];
    }

    private final int value;

    private MessageType(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:MessageType)
  }

  public interface MessageOrBuilder extends
      // @@protoc_insertion_point(interface_extends:Message)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>int32 length = 1;</code>
     */
    int getLength();

    /**
     * <code>.MessageType type = 2;</code>
     */
    int getTypeValue();
    /**
     * <code>.MessageType type = 2;</code>
     */
    MessageProbuf.MessageType getType();

    /**
     * <code>string requestid = 3;</code>
     */
    String getRequestid();
    /**
     * <code>string requestid = 3;</code>
     */
    com.google.protobuf.ByteString
        getRequestidBytes();

    /**
     * <code>string body = 4;</code>
     */
    String getBody();
    /**
     * <code>string body = 4;</code>
     */
    com.google.protobuf.ByteString
        getBodyBytes();

    /**
     * <code>string extend = 5;</code>
     */
    String getExtend();
    /**
     * <code>string extend = 5;</code>
     */
    com.google.protobuf.ByteString
        getExtendBytes();
  }
  /**
   * Protobuf type {@code Message}
   */
  public  static final class Message extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:Message)
      MessageOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use Message.newBuilder() to construct.
    private Message(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private Message() {
      length_ = 0;
      type_ = 0;
      requestid_ = "";
      body_ = "";
      extend_ = "";
    }

    @Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private Message(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      if (extensionRegistry == null) {
        throw new NullPointerException();
      }
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            case 8: {

              length_ = input.readInt32();
              break;
            }
            case 16: {
              int rawValue = input.readEnum();

              type_ = rawValue;
              break;
            }
            case 26: {
              String s = input.readStringRequireUtf8();

              requestid_ = s;
              break;
            }
            case 34: {
              String s = input.readStringRequireUtf8();

              body_ = s;
              break;
            }
            case 42: {
              String s = input.readStringRequireUtf8();

              extend_ = s;
              break;
            }
            default: {
              if (!parseUnknownFieldProto3(
                  input, unknownFields, extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return MessageProbuf.internal_static_Message_descriptor;
    }

    @Override
    protected FieldAccessorTable
        internalGetFieldAccessorTable() {
      return MessageProbuf.internal_static_Message_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              MessageProbuf.Message.class, MessageProbuf.Message.Builder.class);
    }

    public static final int LENGTH_FIELD_NUMBER = 1;
    private int length_;
    /**
     * <code>int32 length = 1;</code>
     */
    public int getLength() {
      return length_;
    }

    public static final int TYPE_FIELD_NUMBER = 2;
    private int type_;
    /**
     * <code>.MessageType type = 2;</code>
     */
    public int getTypeValue() {
      return type_;
    }
    /**
     * <code>.MessageType type = 2;</code>
     */
    public MessageProbuf.MessageType getType() {
      @SuppressWarnings("deprecation")
      MessageProbuf.MessageType result = MessageProbuf.MessageType.valueOf(type_);
      return result == null ? MessageProbuf.MessageType.UNRECOGNIZED : result;
    }

    public static final int REQUESTID_FIELD_NUMBER = 3;
    private volatile Object requestid_;
    /**
     * <code>string requestid = 3;</code>
     */
    public String getRequestid() {
      Object ref = requestid_;
      if (ref instanceof String) {
        return (String) ref;
      } else {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        requestid_ = s;
        return s;
      }
    }
    /**
     * <code>string requestid = 3;</code>
     */
    public com.google.protobuf.ByteString
        getRequestidBytes() {
      Object ref = requestid_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8(
                (String) ref);
        requestid_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int BODY_FIELD_NUMBER = 4;
    private volatile Object body_;
    /**
     * <code>string body = 4;</code>
     */
    public String getBody() {
      Object ref = body_;
      if (ref instanceof String) {
        return (String) ref;
      } else {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        body_ = s;
        return s;
      }
    }
    /**
     * <code>string body = 4;</code>
     */
    public com.google.protobuf.ByteString
        getBodyBytes() {
      Object ref = body_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8(
                (String) ref);
        body_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int EXTEND_FIELD_NUMBER = 5;
    private volatile Object extend_;
    /**
     * <code>string extend = 5;</code>
     */
    public String getExtend() {
      Object ref = extend_;
      if (ref instanceof String) {
        return (String) ref;
      } else {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        extend_ = s;
        return s;
      }
    }
    /**
     * <code>string extend = 5;</code>
     */
    public com.google.protobuf.ByteString
        getExtendBytes() {
      Object ref = extend_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8(
                (String) ref);
        extend_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    private byte memoizedIsInitialized = -1;
    @Override
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    @Override
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (length_ != 0) {
        output.writeInt32(1, length_);
      }
      if (type_ != MessageProbuf.MessageType.CONNECTION.getNumber()) {
        output.writeEnum(2, type_);
      }
      if (!getRequestidBytes().isEmpty()) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 3, requestid_);
      }
      if (!getBodyBytes().isEmpty()) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 4, body_);
      }
      if (!getExtendBytes().isEmpty()) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 5, extend_);
      }
      unknownFields.writeTo(output);
    }

    @Override
    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (length_ != 0) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(1, length_);
      }
      if (type_ != MessageProbuf.MessageType.CONNECTION.getNumber()) {
        size += com.google.protobuf.CodedOutputStream
          .computeEnumSize(2, type_);
      }
      if (!getRequestidBytes().isEmpty()) {
        size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, requestid_);
      }
      if (!getBodyBytes().isEmpty()) {
        size += com.google.protobuf.GeneratedMessageV3.computeStringSize(4, body_);
      }
      if (!getExtendBytes().isEmpty()) {
        size += com.google.protobuf.GeneratedMessageV3.computeStringSize(5, extend_);
      }
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @Override
    public boolean equals(final Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof MessageProbuf.Message)) {
        return super.equals(obj);
      }
      MessageProbuf.Message other = (MessageProbuf.Message) obj;

      boolean result = true;
      result = result && (getLength()
          == other.getLength());
      result = result && type_ == other.type_;
      result = result && getRequestid()
          .equals(other.getRequestid());
      result = result && getBody()
          .equals(other.getBody());
      result = result && getExtend()
          .equals(other.getExtend());
      result = result && unknownFields.equals(other.unknownFields);
      return result;
    }

    @Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      hash = (37 * hash) + LENGTH_FIELD_NUMBER;
      hash = (53 * hash) + getLength();
      hash = (37 * hash) + TYPE_FIELD_NUMBER;
      hash = (53 * hash) + type_;
      hash = (37 * hash) + REQUESTID_FIELD_NUMBER;
      hash = (53 * hash) + getRequestid().hashCode();
      hash = (37 * hash) + BODY_FIELD_NUMBER;
      hash = (53 * hash) + getBody().hashCode();
      hash = (37 * hash) + EXTEND_FIELD_NUMBER;
      hash = (53 * hash) + getExtend().hashCode();
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static MessageProbuf.Message parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static MessageProbuf.Message parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static MessageProbuf.Message parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static MessageProbuf.Message parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static MessageProbuf.Message parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static MessageProbuf.Message parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static MessageProbuf.Message parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static MessageProbuf.Message parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static MessageProbuf.Message parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static MessageProbuf.Message parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static MessageProbuf.Message parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static MessageProbuf.Message parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    @Override
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(MessageProbuf.Message prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    @Override
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @Override
    protected Builder newBuilderForType(
        BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code Message}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:Message)
        MessageProbuf.MessageOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return MessageProbuf.internal_static_Message_descriptor;
      }

      @Override
      protected FieldAccessorTable
          internalGetFieldAccessorTable() {
        return MessageProbuf.internal_static_Message_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                MessageProbuf.Message.class, MessageProbuf.Message.Builder.class);
      }

      // Construct using MessageProbuf.Message.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
        }
      }
      @Override
      public Builder clear() {
        super.clear();
        length_ = 0;

        type_ = 0;

        requestid_ = "";

        body_ = "";

        extend_ = "";

        return this;
      }

      @Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return MessageProbuf.internal_static_Message_descriptor;
      }

      @Override
      public MessageProbuf.Message getDefaultInstanceForType() {
        return MessageProbuf.Message.getDefaultInstance();
      }

      @Override
      public MessageProbuf.Message build() {
        MessageProbuf.Message result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @Override
      public MessageProbuf.Message buildPartial() {
        MessageProbuf.Message result = new MessageProbuf.Message(this);
        result.length_ = length_;
        result.type_ = type_;
        result.requestid_ = requestid_;
        result.body_ = body_;
        result.extend_ = extend_;
        onBuilt();
        return result;
      }

      @Override
      public Builder clone() {
        return (Builder) super.clone();
      }
      @Override
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          Object value) {
        return (Builder) super.setField(field, value);
      }
      @Override
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return (Builder) super.clearField(field);
      }
      @Override
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return (Builder) super.clearOneof(oneof);
      }
      @Override
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, Object value) {
        return (Builder) super.setRepeatedField(field, index, value);
      }
      @Override
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          Object value) {
        return (Builder) super.addRepeatedField(field, value);
      }
      @Override
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof MessageProbuf.Message) {
          return mergeFrom((MessageProbuf.Message)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(MessageProbuf.Message other) {
        if (other == MessageProbuf.Message.getDefaultInstance()) return this;
        if (other.getLength() != 0) {
          setLength(other.getLength());
        }
        if (other.type_ != 0) {
          setTypeValue(other.getTypeValue());
        }
        if (!other.getRequestid().isEmpty()) {
          requestid_ = other.requestid_;
          onChanged();
        }
        if (!other.getBody().isEmpty()) {
          body_ = other.body_;
          onChanged();
        }
        if (!other.getExtend().isEmpty()) {
          extend_ = other.extend_;
          onChanged();
        }
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      @Override
      public final boolean isInitialized() {
        return true;
      }

      @Override
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        MessageProbuf.Message parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (MessageProbuf.Message) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }

      private int length_ ;
      /**
       * <code>int32 length = 1;</code>
       */
      public int getLength() {
        return length_;
      }
      /**
       * <code>int32 length = 1;</code>
       */
      public Builder setLength(int value) {

        length_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>int32 length = 1;</code>
       */
      public Builder clearLength() {

        length_ = 0;
        onChanged();
        return this;
      }

      private int type_ = 0;
      /**
       * <code>.MessageType type = 2;</code>
       */
      public int getTypeValue() {
        return type_;
      }
      /**
       * <code>.MessageType type = 2;</code>
       */
      public Builder setTypeValue(int value) {
        type_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>.MessageType type = 2;</code>
       */
      public MessageProbuf.MessageType getType() {
        @SuppressWarnings("deprecation")
        MessageProbuf.MessageType result = MessageProbuf.MessageType.valueOf(type_);
        return result == null ? MessageProbuf.MessageType.UNRECOGNIZED : result;
      }
      /**
       * <code>.MessageType type = 2;</code>
       */
      public Builder setType(MessageProbuf.MessageType value) {
        if (value == null) {
          throw new NullPointerException();
        }

        type_ = value.getNumber();
        onChanged();
        return this;
      }
      /**
       * <code>.MessageType type = 2;</code>
       */
      public Builder clearType() {

        type_ = 0;
        onChanged();
        return this;
      }

      private Object requestid_ = "";
      /**
       * <code>string requestid = 3;</code>
       */
      public String getRequestid() {
        Object ref = requestid_;
        if (!(ref instanceof String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          String s = bs.toStringUtf8();
          requestid_ = s;
          return s;
        } else {
          return (String) ref;
        }
      }
      /**
       * <code>string requestid = 3;</code>
       */
      public com.google.protobuf.ByteString
          getRequestidBytes() {
        Object ref = requestid_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b =
              com.google.protobuf.ByteString.copyFromUtf8(
                  (String) ref);
          requestid_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>string requestid = 3;</code>
       */
      public Builder setRequestid(
          String value) {
        if (value == null) {
    throw new NullPointerException();
  }

        requestid_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>string requestid = 3;</code>
       */
      public Builder clearRequestid() {

        requestid_ = getDefaultInstance().getRequestid();
        onChanged();
        return this;
      }
      /**
       * <code>string requestid = 3;</code>
       */
      public Builder setRequestidBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);

        requestid_ = value;
        onChanged();
        return this;
      }

      private Object body_ = "";
      /**
       * <code>string body = 4;</code>
       */
      public String getBody() {
        Object ref = body_;
        if (!(ref instanceof String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          String s = bs.toStringUtf8();
          body_ = s;
          return s;
        } else {
          return (String) ref;
        }
      }
      /**
       * <code>string body = 4;</code>
       */
      public com.google.protobuf.ByteString
          getBodyBytes() {
        Object ref = body_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b =
              com.google.protobuf.ByteString.copyFromUtf8(
                  (String) ref);
          body_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>string body = 4;</code>
       */
      public Builder setBody(
          String value) {
        if (value == null) {
    throw new NullPointerException();
  }

        body_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>string body = 4;</code>
       */
      public Builder clearBody() {

        body_ = getDefaultInstance().getBody();
        onChanged();
        return this;
      }
      /**
       * <code>string body = 4;</code>
       */
      public Builder setBodyBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);

        body_ = value;
        onChanged();
        return this;
      }

      private Object extend_ = "";
      /**
       * <code>string extend = 5;</code>
       */
      public String getExtend() {
        Object ref = extend_;
        if (!(ref instanceof String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          String s = bs.toStringUtf8();
          extend_ = s;
          return s;
        } else {
          return (String) ref;
        }
      }
      /**
       * <code>string extend = 5;</code>
       */
      public com.google.protobuf.ByteString
          getExtendBytes() {
        Object ref = extend_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b =
              com.google.protobuf.ByteString.copyFromUtf8(
                  (String) ref);
          extend_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>string extend = 5;</code>
       */
      public Builder setExtend(
          String value) {
        if (value == null) {
    throw new NullPointerException();
  }

        extend_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>string extend = 5;</code>
       */
      public Builder clearExtend() {

        extend_ = getDefaultInstance().getExtend();
        onChanged();
        return this;
      }
      /**
       * <code>string extend = 5;</code>
       */
      public Builder setExtendBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);

        extend_ = value;
        onChanged();
        return this;
      }
      @Override
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFieldsProto3(unknownFields);
      }

      @Override
      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:Message)
    }

    // @@protoc_insertion_point(class_scope:Message)
    private static final MessageProbuf.Message DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new MessageProbuf.Message();
    }

    public static MessageProbuf.Message getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<Message>
        PARSER = new com.google.protobuf.AbstractParser<Message>() {
      @Override
      public Message parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new Message(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<Message> parser() {
      return PARSER;
    }

    @Override
    public com.google.protobuf.Parser<Message> getParserForType() {
      return PARSER;
    }

    @Override
    public MessageProbuf.Message getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_Message_descriptor;
  private static final
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_Message_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    String[] descriptorData = {
      "\n\rmessage.proto\"f\n\007Message\022\016\n\006length\030\001 \001" +
      "(\005\022\032\n\004type\030\002 \001(\0162\014.MessageType\022\021\n\treques" +
      "tid\030\003 \001(\t\022\014\n\004body\030\004 \001(\t\022\016\n\006extend\030\005 \001(\t*" +
      "z\n\013MessageType\022\016\n\nCONNECTION\020\000\022\n\n\006HEALTH" +
      "\020\001\022\t\n\005CLOSE\020\002\022\t\n\005VALID\020\003\022\t\n\005CHECK\020\004\022\n\n\006R" +
      "ESULT\020\005\022\013\n\007REFRESH\020\006\022\n\n\006LOGOUT\020\007\022\t\n\005LOGI" +
      "N\020\010B\017B\rMessageProbufb\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
    internal_static_Message_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_Message_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_Message_descriptor,
        new String[] { "Length", "Type", "Requestid", "Body", "Extend", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
