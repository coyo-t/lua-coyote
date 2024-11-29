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
	;

	operator fun invoke (
		mm:Boolean=false,
		ot:Boolean=false,
		it:Boolean=false,
		t:Boolean=false,
		a:Boolean=false,
	):OpModeBits
	{
		return OpModeBits(
			mm=if (mm) 1 else 0,
			ot=if (ot) 1 else 0,
			it=if (it) 1 else 0,
			t=if (t) 1 else 0,
			a=if (a) 1 else 0,
			this
		)
	}
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

	val mode get() = flags and 7
	val aMode get() = (flags and (1 shl 3)) != 0
	val tMode get() = (flags and (1 shl 4)) != 0
	val itMode get() = (flags and (1 shl 5)) != 0
	val otMode get() = (flags and (1 shl 6)) != 0
	val mmMode get() = (flags and (1 shl 7)) != 0
}

enum class OpCode(val id:Int, val mode:OpModeBits)
{
	MOVE      (0x00, OpMode.iABC(a=true)),
	LOADI     (0x01, OpMode.iAsBx(a=true)),
	LOADF     (0x02, OpMode.iAsBx(a=true)),
	LOADK     (0x03, OpMode.iABx(a=true)),
	LOADKX    (0x04, OpMode.iABx(a=true)),
	LOADFALSE (0x05, OpMode.iABC(a=true)),
	LFALSESKIP(0x06, OpMode.iABC(a=true)),
	LOADTRUE  (0x07, OpMode.iABC(a=true)),
	LOADNIL   (0x08, OpMode.iABC(a=true)),
	GETUPVAL  (0x09, OpMode.iABC(a=true)),
	SETUPVAL  (0x0A, OpMode.iABC()),
	GETTABUP  (0x0B, OpMode.iABC(a=true)),
	GETTABLE  (0x0C, OpMode.iABC(a=true)),
	GETI      (0x0D, OpMode.iABC(a=true)),
	GETFIELD  (0x0E, OpMode.iABC(a=true)),
	SETTABUP  (0x0F, OpMode.iABC()),
	SETTABLE  (0x10, OpMode.iABC()),
	SETI      (0x11, OpMode.iABC()),
	SETFIELD  (0x12, OpMode.iABC()),
	NEWTABLE  (0x13, OpMode.iABC(a=true)),
	SELF      (0x14, OpMode.iABC(a=true)),
	ADDI      (0x15, OpMode.iABC(a=true)),
	ADDK      (0x16, OpMode.iABC(a=true)),
	SUBK      (0x17, OpMode.iABC(a=true)),
	MULK      (0x18, OpMode.iABC(a=true)),
	MODK      (0x19, OpMode.iABC(a=true)),
	POWK      (0x1A, OpMode.iABC(a=true)),
	DIVK      (0x1B, OpMode.iABC(a=true)),
	IDIVK     (0x1C, OpMode.iABC(a=true)),
	BANDK     (0x1D, OpMode.iABC(a=true)),
	BORK      (0x1E, OpMode.iABC(a=true)),
	BXORK     (0x1F, OpMode.iABC(a=true)),
	SHRI      (0x20, OpMode.iABC(a=true)),
	SHLI      (0x21, OpMode.iABC(a=true)),
	ADD       (0x22, OpMode.iABC(a=true)),
	SUB       (0x23, OpMode.iABC(a=true)),
	MUL       (0x24, OpMode.iABC(a=true)),
	MOD       (0x25, OpMode.iABC(a=true)),
	POW       (0x26, OpMode.iABC(a=true)),
	DIV       (0x27, OpMode.iABC(a=true)),
	IDIV      (0x28, OpMode.iABC(a=true)),
	BAND      (0x29, OpMode.iABC(a=true)),
	BOR       (0x2A, OpMode.iABC(a=true)),
	BXOR      (0x2B, OpMode.iABC(a=true)),
	SHL       (0x2C, OpMode.iABC(a=true)),
	SHR       (0x2D, OpMode.iABC(a=true)),
	MMBIN     (0x2E, OpMode.iABC(mm=true)),
	MMBINI    (0x2F, OpMode.iABC(mm=true)),
	MMBINK    (0x30, OpMode.iABC(mm=true)),
	UNM       (0x31, OpMode.iABC(a=true)),
	BNOT      (0x32, OpMode.iABC(a=true)),
	NOT       (0x33, OpMode.iABC(a=true)),
	LEN       (0x34, OpMode.iABC(a=true)),
	CONCAT    (0x35, OpMode.iABC(a=true)),
	CLOSE     (0x36, OpMode.iABC()),
	TBC       (0x37, OpMode.iABC()),
	JMP       (0x38, OpMode.isJ()),
	EQ        (0x39, OpMode.iABC(t=true)),
	LT        (0x3A, OpMode.iABC(t=true)),
	LE        (0x3B, OpMode.iABC(t=true)),
	EQK       (0x3C, OpMode.iABC(t=true)),
	EQI       (0x3D, OpMode.iABC(t=true)),
	LTI       (0x3E, OpMode.iABC(t=true)),
	LEI       (0x3F, OpMode.iABC(t=true)),
	GTI       (0x40, OpMode.iABC(t=true)),
	GEI       (0x41, OpMode.iABC(t=true)),
	TEST      (0x42, OpMode.iABC(t=true)),
	TESTSET   (0x43, OpMode.iABC(t=true,a=true)),
	CALL      (0x44, OpMode.iABC(ot=true,it=true,a=true)),
	TAILCALL  (0x45, OpMode.iABC(ot=true,it=true,a=true)),
	RETURN    (0x46, OpMode.iABC(it=true)),
	RETURN0   (0x47, OpMode.iABC()),
	RETURN1   (0x48, OpMode.iABC()),
	FORLOOP   (0x49, OpMode.iABx(a=true)),
	FORPREP   (0x4A, OpMode.iABx(a=true)),
	TFORPREP  (0x4B, OpMode.iABx()),
	TFORCALL  (0x4C, OpMode.iABC()),
	TFORLOOP  (0x4D, OpMode.iABx(a=true)),
	SETLIST   (0x4E, OpMode.iABC(it=true)),
	CLOSURE   (0x4F, OpMode.iABx(a=true)),
	VARARG    (0x50, OpMode.iABC(ot=true,a=true)),
	VARARGPREP(0x51, OpMode.iABC(it=true,a=true)),
	EXTRAARG  (0x52, OpMode.iAx()),
	;
}
