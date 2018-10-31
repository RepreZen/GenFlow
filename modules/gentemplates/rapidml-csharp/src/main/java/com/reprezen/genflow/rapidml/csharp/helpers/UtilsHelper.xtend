package com.reprezen.genflow.rapidml.csharp.helpers

import com.reprezen.genflow.api.util.GeneratorProperties

class UtilsHelper {
    
    def static generatedAttr() {
        val product = "RepreZen™ GenFlow Framework"
        val version = GeneratorProperties.genFlowVersion
        '''[GeneratedCode("«product»", "«version»")]'''
    }
}
