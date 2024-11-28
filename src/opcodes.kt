/*
  We assume that instructions are unsigned 32-bit integers.
  All instructions have an opcode in the first 7 bits.
  Instructions can have the following formats:

        3 3 2 2 2 2 2 2 2 2 2 2 1 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 0 0
        1 0 9 8 7 6 5 4 3 2 1 0 9 8 7 6 5 4 3 2 1 0 9 8 7 6 5 4 3 2 1 0
iABC          C(8)     |      B(8)     |k|     A(8)      |   Op(7)     |
iABx                Bx(17)               |     A(8)      |   Op(7)     |
iAsBx              sBx (signed)(17)      |     A(8)      |   Op(7)     |
iAx                           Ax(25)                     |   Op(7)     |
isJ                           sJ (signed)(25)            |   Op(7)     |

  A signed argument is represented in excess K: the represented value is
  the written unsigned value minus K, where K is half the maximum for the
  corresponding unsigned argument.
*/


// basic instruction formats
enum class OpMode {
	iABC,
	iABx,
	iAsBx,
	iAx,
	isJ,
}

const val SIZE_C = 8
const val SIZE_B = 8
const val SIZE_Bx = SIZE_C + SIZE_B + 1
const val SIZE_A = 8
const val SIZE_Ax = SIZE_Bx + SIZE_A
const val SIZE_sJ = SIZE_Bx + SIZE_A

const val SIZE_OP = 7

const val POS_OP = 0

const val POS_A = POS_OP + SIZE_OP
const val POS_k = POS_A + SIZE_A
const val POS_B = POS_k + 1
const val POS_C = POS_B + SIZE_B

const val POS_Bx = POS_k

const val POS_Ax = POS_A

const val POS_sJ = POS_A

@JvmInline
value class OpModeBits (val flags:Int)
{
	constructor (mm:Int,ot:Int,it:Int,t:Int,a:Int,m:OpMode): this(
		(mm shl 7) or
		(ot shl 6) or
		(it shl 5) or
		(t shl 4) or
		(a shl 3) or
		(m.ordinal)
	)
}

enum class OpCode(val id:Int, val mode:OpModeBits)
{
	MOVE      (0x00, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	LOADI     (0x01, OpModeBits(0, 0, 0, 0, 1, OpMode.iAsBx)),
	LOADF     (0x02, OpModeBits(0, 0, 0, 0, 1, OpMode.iAsBx)),
	LOADK     (0x03, OpModeBits(0, 0, 0, 0, 1, OpMode.iABx)),
	LOADKX    (0x04, OpModeBits(0, 0, 0, 0, 1, OpMode.iABx)),
	LOADFALSE (0x05, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	LFALSESKIP(0x06, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	LOADTRUE  (0x07, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	LOADNIL   (0x08, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	GETUPVAL  (0x09, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	SETUPVAL  (0x0A, OpModeBits(0, 0, 0, 0, 0, OpMode.iABC)),
	GETTABUP  (0x0B, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	GETTABLE  (0x0C, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	GETI      (0x0D, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	GETFIELD  (0x0E, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	SETTABUP  (0x0F, OpModeBits(0, 0, 0, 0, 0, OpMode.iABC)),
	SETTABLE  (0x10, OpModeBits(0, 0, 0, 0, 0, OpMode.iABC)),
	SETI      (0x11, OpModeBits(0, 0, 0, 0, 0, OpMode.iABC)),
	SETFIELD  (0x12, OpModeBits(0, 0, 0, 0, 0, OpMode.iABC)),
	NEWTABLE  (0x13, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	SELF      (0x14, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	ADDI      (0x15, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	ADDK      (0x16, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	SUBK      (0x17, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	MULK      (0x18, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	MODK      (0x19, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	POWK      (0x1A, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	DIVK      (0x1B, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	IDIVK     (0x1C, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	BANDK     (0x1D, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	BORK      (0x1E, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	BXORK     (0x1F, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	SHRI      (0x20, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	SHLI      (0x21, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	ADD       (0x22, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	SUB       (0x23, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	MUL       (0x24, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	MOD       (0x25, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	POW       (0x26, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	DIV       (0x27, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	IDIV      (0x28, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	BAND      (0x29, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	BOR       (0x2A, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	BXOR      (0x2B, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	SHL       (0x2C, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	SHR       (0x2D, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	MMBIN     (0x2E, OpModeBits(1, 0, 0, 0, 0, OpMode.iABC)),
	MMBINI    (0x2F, OpModeBits(1, 0, 0, 0, 0, OpMode.iABC)),
	MMBINK    (0x30, OpModeBits(1, 0, 0, 0, 0, OpMode.iABC)),
	UNM       (0x31, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	BNOT      (0x32, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	NOT       (0x33, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	LEN       (0x34, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	CONCAT    (0x35, OpModeBits(0, 0, 0, 0, 1, OpMode.iABC)),
	CLOSE     (0x36, OpModeBits(0, 0, 0, 0, 0, OpMode.iABC)),
	TBC       (0x37, OpModeBits(0, 0, 0, 0, 0, OpMode.iABC)),
	JMP       (0x38, OpModeBits(0, 0, 0, 0, 0, OpMode.isJ)),
	EQ        (0x39, OpModeBits(0, 0, 0, 1, 0, OpMode.iABC)),
	LT        (0x3A, OpModeBits(0, 0, 0, 1, 0, OpMode.iABC)),
	LE        (0x3B, OpModeBits(0, 0, 0, 1, 0, OpMode.iABC)),
	EQK       (0x3C, OpModeBits(0, 0, 0, 1, 0, OpMode.iABC)),
	EQI       (0x3D, OpModeBits(0, 0, 0, 1, 0, OpMode.iABC)),
	LTI       (0x3E, OpModeBits(0, 0, 0, 1, 0, OpMode.iABC)),
	LEI       (0x3F, OpModeBits(0, 0, 0, 1, 0, OpMode.iABC)),
	GTI       (0x40, OpModeBits(0, 0, 0, 1, 0, OpMode.iABC)),
	GEI       (0x41, OpModeBits(0, 0, 0, 1, 0, OpMode.iABC)),
	TEST      (0x42, OpModeBits(0, 0, 0, 1, 0, OpMode.iABC)),
	TESTSET   (0x43, OpModeBits(0, 0, 0, 1, 1, OpMode.iABC)),
	CALL      (0x44, OpModeBits(0, 1, 1, 0, 1, OpMode.iABC)),
	TAILCALL  (0x45, OpModeBits(0, 1, 1, 0, 1, OpMode.iABC)),
	RETURN    (0x46, OpModeBits(0, 0, 1, 0, 0, OpMode.iABC)),
	RETURN0   (0x47, OpModeBits(0, 0, 0, 0, 0, OpMode.iABC)),
	RETURN1   (0x48, OpModeBits(0, 0, 0, 0, 0, OpMode.iABC)),
	FORLOOP   (0x49, OpModeBits(0, 0, 0, 0, 1, OpMode.iABx)),
	FORPREP   (0x4A, OpModeBits(0, 0, 0, 0, 1, OpMode.iABx)),
	TFORPREP  (0x4B, OpModeBits(0, 0, 0, 0, 0, OpMode.iABx)),
	TFORCALL  (0x4C, OpModeBits(0, 0, 0, 0, 0, OpMode.iABC)),
	TFORLOOP  (0x4D, OpModeBits(0, 0, 0, 0, 1, OpMode.iABx)),
	SETLIST   (0x4E, OpModeBits(0, 0, 1, 0, 0, OpMode.iABC)),
	CLOSURE   (0x4F, OpModeBits(0, 0, 0, 0, 1, OpMode.iABx)),
	VARARG    (0x50, OpModeBits(0, 1, 0, 0, 1, OpMode.iABC)),
	VARARGPREP(0x51, OpModeBits(0, 0, 1, 0, 1, OpMode.iABC)),
	EXTRAARG  (0x52, OpModeBits(0, 0, 0, 0, 0, OpMode.iAx)),
	;
}
