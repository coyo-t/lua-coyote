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



enum class OpCode(val id:Int)
{
	MOVE      (0x00, ),
	LOADI     (0x01, ),
	LOADF     (0x02, ),
	LOADK     (0x03, ),
	LOADKX    (0x04, ),
	LOADFALSE (0x05, ),
	LFALSESKIP(0x06, ),
	LOADTRUE  (0x07, ),
	LOADNIL   (0x08, ),
	GETUPVAL  (0x09, ),
	SETUPVAL  (0x0A, ),
	GETTABUP  (0x0B, ),
	GETTABLE  (0x0C, ),
	GETI      (0x0D, ),
	GETFIELD  (0x0E, ),
	SETTABUP  (0x0F, ),
	SETTABLE  (0x10, ),
	SETI      (0x11, ),
	SETFIELD  (0x12, ),
	NEWTABLE  (0x13, ),
	SELF      (0x14, ),
	ADDI      (0x15, ),
	ADDK      (0x16, ),
	SUBK      (0x17, ),
	MULK      (0x18, ),
	MODK      (0x19, ),
	POWK      (0x1A, ),
	DIVK      (0x1B, ),
	IDIVK     (0x1C, ),
	BANDK     (0x1D, ),
	BORK      (0x1E, ),
	BXORK     (0x1F, ),
	SHRI      (0x20, ),
	SHLI      (0x21, ),
	ADD       (0x22, ),
	SUB       (0x23, ),
	MUL       (0x24, ),
	MOD       (0x25, ),
	POW       (0x26, ),
	DIV       (0x27, ),
	IDIV      (0x28, ),
	BAND      (0x29, ),
	BOR       (0x2A, ),
	BXOR      (0x2B, ),
	SHL       (0x2C, ),
	SHR       (0x2D, ),
	MMBIN     (0x2E, ),
	MMBINI    (0x2F, ),
	MMBINK    (0x30, ),
	UNM       (0x31, ),
	BNOT      (0x32, ),
	NOT       (0x33, ),
	LEN       (0x34, ),
	CONCAT    (0x35, ),
	CLOSE     (0x36, ),
	TBC       (0x37, ),
	JMP       (0x38, ),
	EQ        (0x39, ),
	LT        (0x3A, ),
	LE        (0x3B, ),
	EQK       (0x3C, ),
	EQI       (0x3D, ),
	LTI       (0x3E, ),
	LEI       (0x3F, ),
	GTI       (0x40, ),
	GEI       (0x41, ),
	TEST      (0x42, ),
	TESTSET   (0x43, ),
	CALL      (0x44, ),
	TAILCALL  (0x45, ),
	RETURN    (0x46, ),
	RETURN0   (0x47, ),
	RETURN1   (0x48, ),
	FORLOOP   (0x49, ),
	FORPREP   (0x4A, ),
	TFORPREP  (0x4B, ),
	TFORCALL  (0x4C, ),
	TFORLOOP  (0x4D, ),
	SETLIST   (0x4E, ),
	CLOSURE   (0x4F, ),
	VARARG    (0x50, ),
	VARARGPREP(0x51, ),
	EXTRAARG  (0x52, ),
}
