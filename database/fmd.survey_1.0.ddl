connect remote:localhost/hoolifan_1.0 admin admin;


create class Test;


create property Test.title string;
create property Test.active boolean;

create index Test.active notunique;

disconnect;