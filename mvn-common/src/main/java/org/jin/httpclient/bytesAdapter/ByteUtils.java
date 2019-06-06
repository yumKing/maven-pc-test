package org.jin.httpclient.bytesAdapter;

/**
 * Copyright 2010 The Apache Software Foundation
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.jin.httpclient.enums.Constants;
import org.jin.httpclient.io.RawComparator;
import sun.misc.Unsafe;


/**
 * Utility class that handles byte arrays, conversions to/from other types,
 * comparisons, hash code generation, manufacturing keys for HashMaps or
 * HashSets, etc.
 */
@SuppressWarnings("restriction")
public final class ByteUtils {

	/**
	 * Size of boolean in bytes
	 */
	public static final int SIZEOF_BOOLEAN = Byte.SIZE / Byte.SIZE;

	/**
	 * Size of byte in bytes
	 */
	public static final int SIZEOF_BYTE = SIZEOF_BOOLEAN;

	/**
	 * Size of char in bytes
	 */
	public static final int SIZEOF_CHAR = Character.SIZE / Byte.SIZE;

	/**
	 * Size of double in bytes
	 */
	public static final int SIZEOF_DOUBLE = Double.SIZE / Byte.SIZE;

	/**
	 * Size of float in bytes
	 */
	public static final int SIZEOF_FLOAT = Float.SIZE / Byte.SIZE;

	/**
	 * Size of int in bytes
	 */
	public static final int SIZEOF_INT = Integer.SIZE / Byte.SIZE;

	/**
	 * Size of long in bytes
	 */
	public static final int SIZEOF_LONG = Long.SIZE / Byte.SIZE;

	/**
	 * Size of short in bytes
	 */
	public static final int SIZEOF_SHORT = Short.SIZE / Byte.SIZE;

	/**
	 * Estimate of size cost to pay beyond payload in jvm for instance of byte
	 * []. Estimate based on study of jhat and jprofiler numbers.
	 */
	// JHat says BU is 56 bytes.
	// SizeOf which uses java.lang.instrument says 24 bytes. (3 longs?)
	public static final int ESTIMATED_HEAP_TAX = 16;

	/**
	 * Byte array comparator class.
	 */

	/**
	 * Read byte-array written with a WritableableUtils.vint prefix.
	 * 
	 * @param in
	 *            Input to read from.
	 * @return byte array read off <code>in</code>
	 * @throws java.io.IOException
	 *             e
	 */
	public static byte[] readByteArray(final DataInput in) throws IOException {
		int len = WritableUtils.readVInt(in);
		if (len < 0) {
			throw new NegativeArraySizeException(Integer.toString(len));
		}
		byte[] result = new byte[len];
		in.readFully(result, 0, len);
		return result;
	}

	/**
	 * Read byte-array written with a WritableableUtils.vint prefix. IOException
	 * is converted to a RuntimeException.
	 *
	 * @param in
	 *            Input to read from.
	 * @return byte array read off <code>in</code>
	 */
	public static byte[] readByteArrayThrowsRuntime(final DataInput in) {
		try {
			return readByteArray(in);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Write byte-array with a WritableableUtils.vint prefix.
	 *
	 * @param out
	 *            output stream to be written to
	 * @param b
	 *            array to write
	 * @throws java.io.IOException
	 *             e
	 */
	public static void writeByteArray(final DataOutput out, final byte[] b)
			throws IOException {
		if (b == null) {
			WritableUtils.writeVInt(out, 0);
		} else {
			writeByteArray(out, b, 0, b.length);
		}
	}

	/**
	 * Write byte-array to out with a vint length prefix.
	 *
	 * @param out
	 *            output stream
	 * @param b
	 *            array
	 * @param offset
	 *            offset into array
	 * @param length
	 *            length past offset
	 * @throws java.io.IOException
	 *             e
	 */
	public static void writeByteArray(final DataOutput out, final byte[] b,
			final int offset, final int length) throws IOException {
		WritableUtils.writeVInt(out, length);
		out.write(b, offset, length);
	}

	/**
	 * Write byte-array from src to tgt with a vint length prefix.
	 * 
	 * @param tgt
	 *            target array
	 * @param tgtOffset
	 *            offset into target array
	 * @param src
	 *            source array
	 * @param srcOffset
	 *            source offset
	 * @param srcLength
	 *            source length
	 * @return New offset in src array.
	 */
	public static int writeByteArray(final byte[] tgt, final int tgtOffset,
			final byte[] src, final int srcOffset, final int srcLength) {
		byte[] vint = vintToBytes(srcLength);
		System.arraycopy(vint, 0, tgt, tgtOffset, vint.length);
		int offset = tgtOffset + vint.length;
		System.arraycopy(src, srcOffset, tgt, offset, srcLength);
		return offset + srcLength;
	}

	/**
	 * Put bytes at the specified byte array position.
	 * 
	 * @param tgtBytes
	 *            the byte array
	 * @param tgtOffset
	 *            position in the array
	 * @param srcBytes
	 *            array to write out
	 * @param srcOffset
	 *            source offset
	 * @param srcLength
	 *            source length
	 * @return incremented offset
	 */
	public static int putBytes(byte[] tgtBytes, int tgtOffset, byte[] srcBytes,
			int srcOffset, int srcLength) {
		System.arraycopy(srcBytes, srcOffset, tgtBytes, tgtOffset, srcLength);
		return tgtOffset + srcLength;
	}

	/**
	 * Write a single byte out to the specified byte array position.
	 * 
	 * @param bytes
	 *            the byte array
	 * @param offset
	 *            position in the array
	 * @param b
	 *            byte to write out
	 * @return incremented offset
	 */
	public static int putByte(byte[] bytes, int offset, byte b) {
		bytes[offset] = b;
		return offset + 1;
	}

	/**
	 * Returns a new byte array, copied from the passed ByteBuffer.
	 * 
	 * @param bb
	 *            A ByteBuffer
	 * @return the byte array
	 */
	public static byte[] toBytes(ByteBuffer bb) {
		int length = bb.limit();
		byte[] result = new byte[length];
		System.arraycopy(bb.array(), bb.arrayOffset(), result, 0, length);
		return result;
	}

	/**
	 * @param b
	 *            Presumed UTF-8 encoded byte array.
	 * @return String made from <code>b</code>
	 */
	public static String toString(final byte[] b) {
		if (b == null) {
			return null;
		}
		return toString(b, 0, b.length);
	}

	/**
	 * Joins two byte arrays together using a separator.
	 * 
	 * @param b1
	 *            The first byte array.
	 * @param sep
	 *            The separator to use.
	 * @param b2
	 *            The second byte array.
	 */
	public static String toString(final byte[] b1, String sep, final byte[] b2) {
		return toString(b1, 0, b1.length) + sep + toString(b2, 0, b2.length);
	}

	/**
	 * This method will convert utf8 encoded bytes into a string. If an
	 * UnsupportedEncodingException occurs, this method will eat it and return
	 * null instead.
	 * 
	 * @param b
	 *            Presumed UTF-8 encoded byte array.
	 * @param off
	 *            offset into array
	 * @param len
	 *            length of utf-8 sequence
	 * @return String made from <code>b</code> or null
	 */
	public static String toString(final byte[] b, int off, int len) {
		if (b == null) {
			return null;
		}
		if (len == 0) {
			return "";
		}
		try {
			return new String(b, off, len, Constants.UTF8_ENCODING);
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	/**
	 * Write a printable representation of a byte array.
	 * 
	 * @param b
	 *            byte array
	 * @return string
	 * @see #toStringBinary(byte[], int, int)
	 */
	public static String toStringBinary(final byte[] b) {
		if (b == null)
			return "null";
		return toStringBinary(b, 0, b.length);
	}

	/**
	 * Converts the given byte buffer, from its array offset to its limit, to a
	 * string. The position and the mark are ignored.
	 * 
	 * @param buf
	 *            a byte buffer
	 * @return a string representation of the buffer's binary contents
	 */
	public static String toStringBinary(ByteBuffer buf) {
		if (buf == null)
			return "null";
		return toStringBinary(buf.array(), buf.arrayOffset(), buf.limit());
	}

	/**
	 * Write a printable representation of a byte array. Non-printable
	 * characters are hex escaped in the format \\x%02X, eg: \x00 \x05 etc
	 * 
	 * @param b
	 *            array to write out
	 * @param off
	 *            offset to start at
	 * @param len
	 *            length to write
	 * @return string output
	 */
	public static String toStringBinary(final byte[] b, int off, int len) {
		StringBuilder result = new StringBuilder();
		for (int i = off; i < off + len; ++i) {
			int ch = b[i] & 0xFF;
			if ((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'Z')
					|| (ch >= 'a' && ch <= 'z')
					|| " `~!@#$%^&*()-_=+[]{}|;:'\",.<>/?".indexOf(ch) >= 0) {
				result.append((char) ch);
			} else {
				result.append(String.format("\\x%02X", ch));
			}
		}
		return result.toString();
	}

	private static boolean isHexDigit(char c) {
		return (c >= 'A' && c <= 'F') || (c >= '0' && c <= '9');
	}

	/**
	 * Takes a ASCII digit in the range A-F0-9 and returns the corresponding
	 * integer/ordinal value.
	 * 
	 * @param ch
	 *            The hex digit.
	 * @return The converted hex value as a byte.
	 */
	public static byte toBinaryFromHex(byte ch) {
		if (ch >= 'A' && ch <= 'F')
			return (byte) ((byte) 10 + (byte) (ch - 'A'));
		// else
		return (byte) (ch - '0');
	}

	public static byte[] toBytesBinary(String in) {
		// this may be bigger than we need, but let's be safe.
		byte[] b = new byte[in.length()];
		int size = 0;
		for (int i = 0; i < in.length(); ++i) {
			char ch = in.charAt(i);
			if (ch == '\\' && in.length() > i + 1 && in.charAt(i + 1) == 'x') {
				// ok, take next 2 hex digits.
				char hd1 = in.charAt(i + 2);
				char hd2 = in.charAt(i + 3);

				// they need to be A-F0-9:
				if (!isHexDigit(hd1) || !isHexDigit(hd2)) {
					// bogus escape code, ignore:
					continue;
				}
				// turn hex ASCII digit -> number
				byte d = (byte) ((toBinaryFromHex((byte) hd1) << 4) + toBinaryFromHex((byte) hd2));

				b[size++] = d;
				i += 3; // skip 3
			} else {
				b[size++] = (byte) ch;
			}
		}
		// resize:
		byte[] b2 = new byte[size];
		System.arraycopy(b, 0, b2, 0, size);
		return b2;
	}

	/**
	 * Converts a string to a UTF-8 byte array.
	 * 
	 * @param s
	 *            string
	 * @return the byte array
	 */
	public static byte[] toBytes(String s) {
		try {
			return s.getBytes(Constants.UTF8_ENCODING);
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	/**
	 * Convert a boolean to a byte array. True becomes -1 and false becomes 0.
	 * 
	 * @param b
	 *            value
	 * @return <code>b</code> encoded in a byte array.
	 */
	public static byte[] toBytes(final boolean b) {
		return new byte[] { b ? (byte) -1 : (byte) 0 };
	}

	public static byte[] toBytes(final Boolean b) {
		return toBytes(b.booleanValue());
	}

	public static byte[] toBytes(final AtomicBoolean b) {
		return toBytes(b.get());
	}

	/**
	 * Reverses {@link #toBytes(boolean)}
	 * 
	 * @param b
	 *            array
	 * @return True or false.
	 */
	public static boolean toBoolean(final byte[] b) {
		if (b.length != 1) {
			throw new IllegalArgumentException("Array has wrong size: "
					+ b.length);
		}
		return b[0] != (byte) 0;
	}

	public static boolean toBoolean(final byte b) {
		return b != (byte) 0;
	}

	/**
	 * Convert a long value to a byte array using big-endian.
	 * 
	 * @param val
	 *            value to convert
	 * @return the byte array
	 */
	public static byte[] toBytes(long val) {
		byte[] b = new byte[8];
		for (int i = 7; i > 0; i--) {
			b[i] = (byte) val;
			val >>>= 8;
		}
		b[0] = (byte) val;
		return b;
	}

	public static byte[] toBytes(Long val) {
		return toBytes(val.longValue());
	}

	public static byte[] toBytes(AtomicLong val) {
		return toBytes(val.longValue());
	}

	/**
	 * Converts a byte array to a long value. Reverses {@link #toBytes(long)}
	 * 
	 * @param bytes
	 *            array
	 * @return the long value
	 */
	public static long toLong(byte[] bytes) {
		return toLong(bytes, 0, SIZEOF_LONG);
	}

	/**
	 * Converts a byte array to a long value. Assumes there will be
	 * {@link #SIZEOF_LONG} bytes available.
	 * 
	 * @param bytes
	 *            bytes
	 * @param offset
	 *            offset
	 * @return the long value
	 */
	public static long toLong(byte[] bytes, int offset) {
		return toLong(bytes, offset, SIZEOF_LONG);
	}

	/**
	 * Converts a byte array to a long value.
	 * 
	 * @param bytes
	 *            array of bytes
	 * @param offset
	 *            offset into array
	 * @param length
	 *            length of data (must be {@link #SIZEOF_LONG})
	 * @return the long value
	 * @throws IllegalArgumentException
	 *             if length is not {@link #SIZEOF_LONG} or if there's not
	 *             enough room in the array at the offset indicated.
	 */
	public static long toLong(byte[] bytes, int offset, final int length) {
		if (length != SIZEOF_LONG || offset + length > bytes.length) {
			throw explainWrongLengthOrOffset(bytes, offset, length, SIZEOF_LONG);
		}
		long l = 0;
		for (int i = offset; i < offset + length; i++) {
			l <<= 8;
			l ^= bytes[i] & 0xFF;
		}
		return l;
	}

	private static IllegalArgumentException explainWrongLengthOrOffset(
			final byte[] bytes, final int offset, final int length,
			final int expectedLength) {
		String reason;
		if (length != expectedLength) {
			reason = "Wrong length: " + length + ", expected " + expectedLength;
		} else {
			reason = "offset (" + offset + ") + length (" + length
					+ ") exceed the" + " capacity of the array: "
					+ bytes.length;
		}
		return new IllegalArgumentException(reason);
	}

	/**
	 * Put a long value out to the specified byte array position.
	 * 
	 * @param bytes
	 *            the byte array
	 * @param offset
	 *            position in the array
	 * @param val
	 *            long to write out
	 * @return incremented offset
	 * @throws IllegalArgumentException
	 *             if the byte array given doesn't have enough room at the
	 *             offset specified.
	 */
	public static int putLong(byte[] bytes, int offset, long val) {
		if (bytes.length - offset < SIZEOF_LONG) {
			throw new IllegalArgumentException(
					"Not enough room to put a long at" + " offset " + offset
							+ " in a " + bytes.length + " byte array");
		}
		for (int i = offset + 7; i > offset; i--) {
			bytes[i] = (byte) val;
			val >>>= 8;
		}
		bytes[offset] = (byte) val;
		return offset + SIZEOF_LONG;
	}

	/**
	 * Presumes float encoded as IEEE 754 floating-point "single format"
	 * 
	 * @param bytes
	 *            byte array
	 * @return Float made from passed byte array.
	 */
	public static float toFloat(byte[] bytes) {
		return toFloat(bytes, 0);
	}

	/**
	 * Presumes float encoded as IEEE 754 floating-point "single format"
	 * 
	 * @param bytes
	 *            array to convert
	 * @param offset
	 *            offset into array
	 * @return Float made from passed byte array.
	 */
	public static float toFloat(byte[] bytes, int offset) {
		return Float.intBitsToFloat(toInt(bytes, offset, SIZEOF_INT));
	}

	public static float toFloat(byte[] bytes, int offset, int length) {
		if (length != SIZEOF_FLOAT || offset + length > bytes.length) {
			throw explainWrongLengthOrOffset(bytes, offset, length,
					SIZEOF_FLOAT);
		}
		return Float.intBitsToFloat(toInt(bytes, offset, SIZEOF_INT));
	}

	/**
	 * @param bytes
	 *            byte array
	 * @param offset
	 *            offset to write to
	 * @param f
	 *            float value
	 * @return New offset in <code>bytes</code>
	 */
	public static int putFloat(byte[] bytes, int offset, float f) {
		return putInt(bytes, offset, Float.floatToRawIntBits(f));
	}

	/**
	 * @param f
	 *            float value
	 * @return the float represented as byte []
	 */
	public static byte[] toBytes(final float f) {
		// Encode it as int
		return ByteUtils.toBytes(Float.floatToRawIntBits(f));
	}

	public static byte[] toBytes(final Float f) {
		// Encode it as int
		return toBytes(f.floatValue());
	}

	/**
	 * @param bytes
	 *            byte array
	 * @return Return double made from passed bytes.
	 */
	public static double toDouble(final byte[] bytes) {
		return toDouble(bytes, 0);
	}

	/**
	 * @param bytes
	 *            byte array
	 * @param offset
	 *            offset where double is
	 * @return Return double made from passed bytes.
	 */
	public static double toDouble(final byte[] bytes, final int offset) {
		return Double.longBitsToDouble(toLong(bytes, offset, SIZEOF_LONG));
	}

	public static double toDouble(final byte[] bytes, final int offset,
			int length) {
		if (length != SIZEOF_DOUBLE || offset + length > bytes.length) {
			throw explainWrongLengthOrOffset(bytes, offset, length,
					SIZEOF_DOUBLE);
		}
		return Double.longBitsToDouble(toLong(bytes, offset, SIZEOF_LONG));
	}

	/**
	 * @param bytes
	 *            byte array
	 * @param offset
	 *            offset to write to
	 * @param d
	 *            value
	 * @return New offset into array <code>bytes</code>
	 */
	public static int putDouble(byte[] bytes, int offset, double d) {
		return putLong(bytes, offset, Double.doubleToLongBits(d));
	}

	/**
	 * Serialize a double as the IEEE 754 double format output. The resultant
	 * array will be 8 bytes long.
	 * 
	 * @param d
	 *            value
	 * @return the double represented as byte []
	 */
	public static byte[] toBytes(final double d) {
		// Encode it as a long
		return ByteUtils.toBytes(Double.doubleToRawLongBits(d));
	}

	public static byte[] toBytes(final Double d) {
		// Encode it as a long
		return toBytes(d.doubleValue());
	}

	/**
	 * Convert an int value to a byte array
	 * 
	 * @param val
	 *            value
	 * @return the byte array
	 */
	public static byte[] toBytes(int val) {
		byte[] b = new byte[4];
		for (int i = 3; i > 0; i--) {
			b[i] = (byte) val;
			val >>>= 8;
		}
		b[0] = (byte) val;
		return b;
	}

	public static byte[] toBytes(Integer val) {
		return toBytes(val.intValue());
	}

	public static byte[] toBytes(AtomicInteger val) {
		return toBytes(val.intValue());
	}

	/**
	 * Converts a byte array to an int value
	 * 
	 * @param bytes
	 *            byte array
	 * @return the int value
	 */
	public static int toInt(byte[] bytes) {
		return toInt(bytes, 0, SIZEOF_INT);
	}

	/**
	 * Converts a byte array to an int value
	 * 
	 * @param bytes
	 *            byte array
	 * @param offset
	 *            offset into array
	 * @return the int value
	 */
	public static int toInt(byte[] bytes, int offset) {
		return toInt(bytes, offset, SIZEOF_INT);
	}

	/**
	 * Converts a byte array to an int value
	 * 
	 * @param bytes
	 *            byte array
	 * @param offset
	 *            offset into array
	 * @param length
	 *            length of int (has to be {@link #SIZEOF_INT})
	 * @return the int value
	 * @throws IllegalArgumentException
	 *             if length is not {@link #SIZEOF_INT} or if there's not enough
	 *             room in the array at the offset indicated.
	 */
	public static int toInt(byte[] bytes, int offset, final int length) {
		if (length != SIZEOF_INT || offset + length > bytes.length) {
			throw explainWrongLengthOrOffset(bytes, offset, length, SIZEOF_INT);
		}
		int n = 0;
		for (int i = offset; i < (offset + length); i++) {
			n <<= 8;
			n ^= bytes[i] & 0xFF;
		}
		return n;
	}

	/**
	 * Put an int value out to the specified byte array position.
	 * 
	 * @param bytes
	 *            the byte array
	 * @param offset
	 *            position in the array
	 * @param val
	 *            int to write out
	 * @return incremented offset
	 * @throws IllegalArgumentException
	 *             if the byte array given doesn't have enough room at the
	 *             offset specified.
	 */
	public static int putInt(byte[] bytes, int offset, int val) {
		if (bytes.length - offset < SIZEOF_INT) {
			throw new IllegalArgumentException(
					"Not enough room to put an int at" + " offset " + offset
							+ " in a " + bytes.length + " byte array");
		}
		for (int i = offset + 3; i > offset; i--) {
			bytes[i] = (byte) val;
			val >>>= 8;
		}
		bytes[offset] = (byte) val;
		return offset + SIZEOF_INT;
	}

	/**
	 * Convert a short value to a byte array of {@link #SIZEOF_SHORT} bytes
	 * long.
	 * 
	 * @param val
	 *            value
	 * @return the byte array
	 */
	public static byte[] toBytes(short val) {
		byte[] b = new byte[SIZEOF_SHORT];
		b[1] = (byte) val;
		val >>= 8;
		b[0] = (byte) val;
		return b;
	}

	public static byte[] toBytes(Byte val) {
		return new byte[] { val.byteValue() };
	}

	public static byte[] toBytes(Short val) {
		return toBytes(val.shortValue());
	}

	/**
	 * Converts a byte array to a short value
	 * 
	 * @param bytes
	 *            byte array
	 * @return the short value
	 */
	public static short toShort(byte[] bytes) {
		return toShort(bytes, 0, SIZEOF_SHORT);
	}

	/**
	 * Converts a byte array to a short value
	 * 
	 * @param bytes
	 *            byte array
	 * @param offset
	 *            offset into array
	 * @return the short value
	 */
	public static short toShort(byte[] bytes, int offset) {
		return toShort(bytes, offset, SIZEOF_SHORT);
	}

	/**
	 * Converts a byte array to a short value
	 * 
	 * @param bytes
	 *            byte array
	 * @param offset
	 *            offset into array
	 * @param length
	 *            length, has to be {@link #SIZEOF_SHORT}
	 * @return the short value
	 * @throws IllegalArgumentException
	 *             if length is not {@link #SIZEOF_SHORT} or if there's not
	 *             enough room in the array at the offset indicated.
	 */
	public static short toShort(byte[] bytes, int offset, final int length) {
		if (length != SIZEOF_SHORT || offset + length > bytes.length) {
			throw explainWrongLengthOrOffset(bytes, offset, length,
					SIZEOF_SHORT);
		}
		short n = 0;
		n ^= bytes[offset] & 0xFF;
		n <<= 8;
		n ^= bytes[offset + 1] & 0xFF;
		return n;
	}

	/**
	 * This method will get a sequence of bytes from pos -> limit, but will
	 * restore pos after.
	 * 
	 * @param buf
	 * @return byte array
	 */
	public static byte[] getBytes(ByteBuffer buf) {
		int savedPos = buf.position();
		byte[] newBytes = new byte[buf.remaining()];
		buf.get(newBytes);
		buf.position(savedPos);
		return newBytes;
	}

	/**
	 * Put a short value out to the specified byte array position.
	 * 
	 * @param bytes
	 *            the byte array
	 * @param offset
	 *            position in the array
	 * @param val
	 *            short to write out
	 * @return incremented offset
	 * @throws IllegalArgumentException
	 *             if the byte array given doesn't have enough room at the
	 *             offset specified.
	 */
	public static int putShort(byte[] bytes, int offset, short val) {
		if (bytes.length - offset < SIZEOF_SHORT) {
			throw new IllegalArgumentException(
					"Not enough room to put a short at" + " offset " + offset
							+ " in a " + bytes.length + " byte array");
		}
		bytes[offset + 1] = (byte) val;
		val >>= 8;
		bytes[offset] = (byte) val;
		return offset + SIZEOF_SHORT;
	}

	/**
	 * Convert a BigDecimal value to a byte array
	 * 
	 * @param val
	 * @return the byte array
	 */
	public static byte[] toBytes(BigDecimal val) {
		byte[] valueBytes = val.unscaledValue().toByteArray();
		byte[] result = new byte[valueBytes.length + SIZEOF_INT];
		int offset = putInt(result, 0, val.scale());
		putBytes(result, offset, valueBytes, 0, valueBytes.length);
		return result;
	}

	/**
	 * Converts a byte array to a BigDecimal
	 * 
	 * @param bytes
	 * @return the char value
	 */
	public static BigDecimal toBigDecimal(byte[] bytes) {
		return toBigDecimal(bytes, 0, bytes.length);
	}

	/**
	 * Converts a byte array to a BigDecimal value
	 * 
	 * @param bytes
	 * @param offset
	 * @param length
	 * @return the char value
	 */
	public static BigDecimal toBigDecimal(byte[] bytes, int offset,
			final int length) {
		if (bytes == null || length < SIZEOF_INT + 1
				|| (offset + length > bytes.length)) {
			return null;
		}

		int scale = toInt(bytes, offset);
		byte[] tcBytes = new byte[length - SIZEOF_INT];
		System.arraycopy(bytes, offset + SIZEOF_INT, tcBytes, 0, length
				- SIZEOF_INT);
		return new BigDecimal(new BigInteger(tcBytes), scale);
	}

	/**
	 * Put a BigDecimal value out to the specified byte array position.
	 * 
	 * @param bytes
	 *            the byte array
	 * @param offset
	 *            position in the array
	 * @param val
	 *            BigDecimal to write out
	 * @return incremented offset
	 */
	public static int putBigDecimal(byte[] bytes, int offset, BigDecimal val) {
		if (bytes == null) {
			return offset;
		}

		byte[] valueBytes = val.unscaledValue().toByteArray();
		byte[] result = new byte[valueBytes.length + SIZEOF_INT];
		offset = putInt(result, offset, val.scale());
		return putBytes(result, offset, valueBytes, 0, valueBytes.length);
	}

	/**
	 * @param vint
	 *            Integer to make a vint of.
	 * @return Vint as bytes array.
	 */
	public static byte[] vintToBytes(final long vint) {
		long i = vint;
		int size = WritableUtils.getVIntSize(i);
		byte[] result = new byte[size];
		int offset = 0;
		if (i >= -112 && i <= 127) {
			result[offset] = (byte) i;
			return result;
		}

		int len = -112;
		if (i < 0) {
			i ^= -1L; // take one's complement'
			len = -120;
		}

		long tmp = i;
		while (tmp != 0) {
			tmp = tmp >> 8;
			len--;
		}

		result[offset++] = (byte) len;

		len = (len < -120) ? -(len + 120) : -(len + 112);

		for (int idx = len; idx != 0; idx--) {
			int shiftbits = (idx - 1) * 8;
			long mask = 0xFFL << shiftbits;
			result[offset++] = (byte) ((i & mask) >> shiftbits);
		}
		return result;
	}

	/**
	 * @param buffer
	 *            buffer to convert
	 * @return vint bytes as an integer.
	 */
	public static long bytesToVint(final byte[] buffer) {
		int offset = 0;
		byte firstByte = buffer[offset++];
		int len = WritableUtils.decodeVIntSize(firstByte);
		if (len == 1) {
			return firstByte;
		}
		long i = 0;
		for (int idx = 0; idx < len - 1; idx++) {
			byte b = buffer[offset++];
			i = i << 8;
			i = i | (b & 0xFF);
		}
		return (WritableUtils.isNegativeVInt(firstByte) ? ~i : i);
	}

	/**
	 * Reads a zero-compressed encoded long from input stream and returns it.
	 * 
	 * @param buffer
	 *            Binary array
	 * @param offset
	 *            Offset into array at which vint begins.
	 * @throws java.io.IOException
	 *             e
	 * @return deserialized long from stream.
	 */
	public static long readVLong(final byte[] buffer, final int offset)
			throws IOException {
		byte firstByte = buffer[offset];
		int len = WritableUtils.decodeVIntSize(firstByte);
		if (len == 1) {
			return firstByte;
		}
		long i = 0;
		for (int idx = 0; idx < len - 1; idx++) {
			byte b = buffer[offset + 1 + idx];
			i = i << 8;
			i = i | (b & 0xFF);
		}
		return (WritableUtils.isNegativeVInt(firstByte) ? ~i : i);
	}

	public static String print(byte[] bytes) {
		if (bytes == null)
			return "null";
		int size = bytes.length;
		if (size == 0)
			return "[]";

		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (int i = 0;; i++) {
			sb.append(bytes[i]);
			if (i == (size - 1))
				return sb.append(']').toString();
			sb.append(", ");
		}
	}

	/**
	 * Pass this to TreeMaps where byte [] are keys.
	 */
	public static Comparator<byte[]> BYTES_COMPARATOR = new ByteArrayComparator();

	/**
	 * Use comparing byte arrays, byte-by-byte
	 */
	public static RawComparator<byte[]> BYTES_RAWCOMPARATOR = new ByteArrayComparator();

	/**
	 * Byte array comparator class.
	 */
	public static class ByteArrayComparator implements RawComparator<byte[]> {
		/**
		 * Constructor
		 */
		public ByteArrayComparator() {
			super();
		}

		public int compare(byte[] left, byte[] right) {
			return compareTo(left, right);
		}

		public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
			return LexicographicalComparerHolder.BEST_COMPARER.compareTo(b1,
					s1, l1, b2, s2, l2);
		}
	}

	interface Comparer<T> {
		abstract public int compareTo(T buffer1, int offset1, int length1,
				T buffer2, int offset2, int length2);
	}

	static class LexicographicalComparerHolder {
		static final String UNSAFE_COMPARER_NAME = LexicographicalComparerHolder.class
				.getName() + "$UnsafeComparer";

		static final Comparer<byte[]> BEST_COMPARER = getBestComparer();

		/**
		 * Returns the Unsafe-using Comparer, or falls back to the pure-Java
		 * implementation if unable to do so.
		 */
		static Comparer<byte[]> getBestComparer() {
			try {
				Class<?> theClass = Class.forName(UNSAFE_COMPARER_NAME);

				// yes, UnsafeComparer does implement Comparer<byte[]>
				@SuppressWarnings("unchecked")
				Comparer<byte[]> comparer = (Comparer<byte[]>) theClass
						.getEnumConstants()[0];
				return comparer;
			} catch (Throwable t) { // ensure we really catch *everything*
				return lexicographicalComparerJavaImpl();
			}
		}

		enum PureJavaComparer implements Comparer<byte[]> {
			INSTANCE;

			@Override
			public int compareTo(byte[] buffer1, int offset1, int length1,
					byte[] buffer2, int offset2, int length2) {
				// Short circuit equal case
				if (buffer1 == buffer2 && offset1 == offset2
						&& length1 == length2) {
					return 0;
				}
				// Bring WritableComparator code local
				int end1 = offset1 + length1;
				int end2 = offset2 + length2;
				for (int i = offset1, j = offset2; i < end1 && j < end2; i++, j++) {
					int a = (buffer1[i] & 0xff);
					int b = (buffer2[j] & 0xff);
					if (a != b) {
						return a - b;
					}
				}
				return length1 - length2;
			}
		}

		enum UnsafeComparer implements Comparer<byte[]> {
			INSTANCE;

			static final Unsafe theUnsafe;

			/** The offset to the first element in a byte array. */
			static final int BYTE_ARRAY_BASE_OFFSET;

			static {
				theUnsafe = (Unsafe) AccessController
						.doPrivileged(new PrivilegedAction<Object>() {
							@Override
							public Object run() {
								try {
									Field f = Unsafe.class
											.getDeclaredField("theUnsafe");
									f.setAccessible(true);
									return f.get(null);
								} catch (NoSuchFieldException e) {
									// It doesn't matter what we throw;
									// it's swallowed in getBestComparer().
									throw new Error();
								} catch (IllegalAccessException e) {
									throw new Error();
								}
							}
						});

				BYTE_ARRAY_BASE_OFFSET = theUnsafe
						.arrayBaseOffset(byte[].class);

				// sanity check - this should never fail
				if (theUnsafe.arrayIndexScale(byte[].class) != 1) {
					throw new AssertionError();
				}
			}

			static final boolean littleEndian = ByteOrder.nativeOrder().equals(
					ByteOrder.LITTLE_ENDIAN);

			/**
			 * Returns true if x1 is less than x2, when both values are treated
			 * as unsigned.
			 */
			static boolean lessThanUnsigned(long x1, long x2) {
				return (x1 + Long.MIN_VALUE) < (x2 + Long.MIN_VALUE);
			}

			/**
			 * Lexicographically compare two arrays.
			 * 
			 * @param buffer1
			 *            left operand
			 * @param buffer2
			 *            right operand
			 * @param offset1
			 *            Where to start comparing in the left buffer
			 * @param offset2
			 *            Where to start comparing in the right buffer
			 * @param length1
			 *            How much to compare from the left buffer
			 * @param length2
			 *            How much to compare from the right buffer
			 * @return 0 if equal, < 0 if left is less than right, etc.
			 */
			@Override
			public int compareTo(byte[] buffer1, int offset1, int length1,
					byte[] buffer2, int offset2, int length2) {
				// Short circuit equal case
				if (buffer1 == buffer2 && offset1 == offset2
						&& length1 == length2) {
					return 0;
				}
				int minLength = Math.min(length1, length2);
				int minWords = minLength / SIZEOF_LONG;
				int offset1Adj = offset1 + BYTE_ARRAY_BASE_OFFSET;
				int offset2Adj = offset2 + BYTE_ARRAY_BASE_OFFSET;

				/*
				 * Compare 8 bytes at a time. Benchmarking shows comparing 8
				 * bytes at a time is no slower than comparing 4 bytes at a time
				 * even on 32-bit. On the other hand, it is substantially faster
				 * on 64-bit.
				 */
				for (int i = 0; i < minWords * SIZEOF_LONG; i += SIZEOF_LONG) {
					long lw = theUnsafe.getLong(buffer1, offset1Adj + (long) i);
					long rw = theUnsafe.getLong(buffer2, offset2Adj + (long) i);
					long diff = lw ^ rw;

					if (diff != 0) {
						if (!littleEndian) {
							return lessThanUnsigned(lw, rw) ? -1 : 1;
						}

						// Use binary search
						int n = 0;
						int y;
						int x = (int) diff;
						if (x == 0) {
							x = (int) (diff >>> 32);
							n = 32;
						}

						y = x << 16;
						if (y == 0) {
							n += 16;
						} else {
							x = y;
						}

						y = x << 8;
						if (y == 0) {
							n += 8;
						}
						return (int) (((lw >>> n) & 0xFFL) - ((rw >>> n) & 0xFFL));
					}
				}

				// The epilogue to cover the last (minLength % 8) elements.
				for (int i = minWords * SIZEOF_LONG; i < minLength; i++) {
					int a = (buffer1[offset1 + i] & 0xff);
					int b = (buffer2[offset2 + i] & 0xff);
					if (a != b) {
						return a - b;
					}
				}
				return length1 - length2;
			}
		}
	}

	/**
	 * @param left
	 *            left operand
	 * @param right
	 *            right operand
	 * @return 0 if equal, < 0 if left is less than right, etc.
	 */
	public static int compareTo(final byte[] left, final byte[] right) {
		return LexicographicalComparerHolder.BEST_COMPARER.compareTo(left, 0,
				left.length, right, 0, right.length);
	}

	static Comparer<byte[]> lexicographicalComparerJavaImpl() {
		return LexicographicalComparerHolder.PureJavaComparer.INSTANCE;
	}
}
