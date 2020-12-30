public class Person {
    private int person_id;
    private String name;
    private String address;
    private String phone;
    private int birthYear;
    private String info1;
    private String info2;
    private String info3;

    public Person(String name, int birthYear, String address, String phone, String info1, String info2, String info3){
        this.name = name;
        this.birthYear = birthYear;
        this.address = address;
        this.phone = phone;
        this.info1 = info1;
        this.info2 = info2;
        this.info3 = info3;
    }

    public Person(String name, int birthYear, String address, String phone, String info1, String info2, String info3,
                  int person_id){
        this.name = name;
        this.birthYear = birthYear;
        this.address = address;
        this.phone = phone;
        this.info1 = info1;
        this.info2 = info2;
        this.info3 = info3;
        this.person_id = person_id;
    }

    @Override
    public String toString() {
        return person_id + ": " + name + ", Address: " + address +  ", Phone: " + phone +
                ", Year of Birth: " + birthYear + ", Info 1: " + info1 + ", Info 2: " + info2 +
                ", Info 3: " + info3;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public int getBirthYear() {
        return birthYear;
    }

    public String getInfo1() {
        return info1;
    }

    public String getInfo2() {
        return info2;
    }

    public String getInfo3() {
        return info3;
    }

    public int getPerson_id() {
        return person_id;
    }
}