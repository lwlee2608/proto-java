package io.github.lwlee2608.proto.gen;

import io.github.lwlee2608.proto.annotation.processor.ProtoFile;

import javax.annotation.processing.Filer;
import java.util.List;

public interface ProtoGen {
    void generate(Filer filer, List<ProtoFile> protoFiles);
}
