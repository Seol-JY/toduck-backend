package im.toduck.domain.persistence.entity.person;

public enum Alarm {
	TEN(10),
	TIRTY(30),
	SIXTY(60),
	OFF(0);

	private final int value;

	Alarm(int value) {
		this.value = value;
	}
}
