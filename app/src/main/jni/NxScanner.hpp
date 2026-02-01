#pragma once

#include <vector>
#include <string>
#include <sstream>
#include <iomanip>
#include <cstdint>
#include <cstring>
#include <elf.h>
#include <sys/mman.h>
#include "Includes/Logger.h"
#include "Includes/obfuscate.h"

// NxScanner: Lightweight signature scanner for Android
namespace NxScanner {

    // Helper to parse hex strings "00 AA ?? BB"
    inline std::vector<int> ParseSignature(const std::string& signature) {
        std::vector<int> bytes;
        std::stringstream ss(signature);
        std::string word;
        while (ss >> word) {
            if (word == "?" || word == "??") {
                bytes.push_back(-1); // Wildcard
            } else {
                bytes.push_back(std::strtoul(word.c_str(), nullptr, 16));
            }
        }
        return bytes;
    }

    // Scan memory for signature
    inline uintptr_t Scan(uintptr_t start, size_t length, const std::string& signature) {
        std::vector<int> pattern = ParseSignature(signature);
        uint8_t* pStart = (uint8_t*)start;
        
        for (size_t i = 0; i < length - pattern.size(); ++i) {
            bool found = true;
            for (size_t j = 0; j < pattern.size(); ++j) {
                if (pattern[j] != -1 && pStart[i + j] != pattern[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return start + i;
            }
        }
        return 0;
    }
}

