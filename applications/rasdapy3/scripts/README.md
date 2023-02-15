When the rasnet client/server protocol changes, the generated stubs from the
protobuf message files need to be updated. This can be done with

    python3 stub_generator.py

If that fails for some reason, it could be manually done with:

    cd .. # back up to rasdapy3 dir
    BUILD_DIR=/path/to/rasdaman/build/dir # where you compiled rasdaman

    for f in client_rassrvr_service common_service rasmgr_client_service; do
      $BUILD_DIR/third_party/bin/protobuf/bin/protoc \
        -I ../../rasnet/protomessages/ \
        --python_out=rasdapy/stubs/ \
        --grpc_out=rasdapy/stubs/ \
        --plugin=protoc-gen-grpc=/usr/bin/grpc_python_plugin \
        $f.proto
    done

It requires the protobuf-compiler-grpc package to be installed as it provides
the `grpc_python_plugin` executable.