package th.chanwit

enum MiraAction {
	service,
	machine,
	network,
	/* build */

	build,
	up,
	down,
	push,

	// special action as the return value
	error
}