













abstract class GCObject
{
	var next: GCObject? = null
	var tt = 0
	var marked = false
}


class TString: GCObject()
{
	var extra = 0
	var shortLength = 0
	var hash = 0u

	var uLongLength = 0L
	var uHNext: TString? = null

	var contents = ""
}
