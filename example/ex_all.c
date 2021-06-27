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
        case 1: print 121;
        case 2: print 212;

    }

}