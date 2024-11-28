package lparser

enum class VariableKind
{
	// regular
	VDKREG,
	// constant
	RDKCONST,
	// to-be-closed
	RDKTOCLOSE,
	// compile-time constant
	RDKCTC,
}