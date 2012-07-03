BEGIN {incode = 0}
(incode == 0) && ($0 ~ /<pre>/ ) { print $0; incode = 1; next; }
(incode == 0) && ($0 ~ /<pre caption=.*>/ ) { print $0; incode = 1; next; }
(incode == 1) && ($0 ~ /<\/pre>/ )  { print $0; incode = 0; next; }
(incode == 0) && ($0 ~ /<code>/ ) { print $0; incode = 1; next; }
(incode == 1) && ($0 ~ /<\/code>/ )  { print $0; incode = 0; next; }
(incode == 0) {  print $0; }
(incode == 1) {  gsub(/ /, "\xc2\xa0"); print } # &nbsp; is 0xC2A0 in UTF-8

    
