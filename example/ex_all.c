void main()
{
    int i;

    for (i = 0; i < 5; i = i + 1)
    {
        print i;
    }

    --i;
    print i;

    i *= 3;
    print i;
    
    i = 1>2 ? 4 : 1;
    print i;

    switch (i){
        case 3: i=121;
        case 2: i=212;
        default: i=333;
    }
    print i;

    do {
        print 555;
        i=1;
    } while(i>1);

    do {
        print 0;
        ++i;
    } until(i>3);

    for i in range (2,10,3)
    {
        print i;
    }

    float j;
    j=1.2>1.4?1:2;
    print j;
    // j++;
    // print j;
}