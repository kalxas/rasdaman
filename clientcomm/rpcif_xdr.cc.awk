BEGIN                   { 
                        nodebug = 0;
                        print"#pragma GCC diagnostic ignored \"-Wsign-compare\"";
                        print"#pragma GCC diagnostic ignored \"-Wstrict-aliasing\"";
                        print"#pragma GCC diagnostic ignored \"-Wsign-conversion\"";
                        }
/rpcshutdown/           { nodebug = 1; print $0; next; }
/^}/                    { nodebug = 0; print $0; next; }
nodebug==1              { print $0; next; }
/\) *!= *RPC_SUCCESS/   { x=$0; 
                        sub(/\) *!= *RPC_SUCCESS *\)/, ") ) != RPC_SUCCESS )", x);
                        print x;
                        next;
                        }
/return *\(NULL\)/
                        { print $0; }
END                     {
                        print"#pragma GCC diagnostic warning \"-Wsign-compare\"";
                        print"#pragma GCC diagnostic warning \"-Wstrict-aliasing\"";
                        print"#pragma GCC diagnostic ignored \"-Wsign-conversion\"";
                        }