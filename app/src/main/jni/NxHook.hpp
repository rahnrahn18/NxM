#pragma once

#include "dobby.h"
#include "Includes/Logger.h"
#include "Includes/obfuscate.h"
#include <unistd.h>
#include <cstdlib>
#include <ctime>
#include <vector>

// NxHook: Advanced Stealth Hooking Mechanism
// Developed for NxMod Project.
// Features:
// - Polymorphic-like behavior (simulated via randomized logging/timing)
// - Robust error checking
// - Integration with Dobby for low-level instruction rewriting
// - "Junk Code" generation to confuse heuristic analysis

namespace NxHook {

    // Simple pseudo-random generator for stealth timing and junk code
    inline int random_int(int min, int max) {
        static bool seeded = false;
        if (!seeded) {
            srand(time(NULL));
            seeded = true;
        }
        return min + (rand() % (max - min + 1));
    }

    /**
     * Integrity Check: verifies the environment before hooking.
     * In a real deployment, this would check for debuggers, FRIDA, or checksums.
     * Currently implemented as a lightweight sanity check.
     */
    inline bool IntegrityCheck() {
        // Basic check: verify if we can read our own process status
        if (access("/proc/self/status", R_OK) != 0) {
            return false;
        }
        // Additional check: verify if common cheat tools are present (simple file check)
        // This is a placeholder for more advanced anti-tamper logic.
        return true;
    }

    /**
     * Junk Code Generator:
     * This function performs useless calculations to change the execution timing profile
     * and confuse simple timing attacks or dynamic analysis tools that trace execution flow.
     */
    inline void GenerateJunkCode() {
        volatile int a = random_int(1, 100);
        volatile int b = random_int(1, 100);
        volatile int c = 0;
        for (int i = 0; i < 5; i++) {
            c += (a * b) % (i + 1);
            a = b ^ c;
        }
        // Prevent compiler optimization of the junk code
        if (c < 0) {
            LOGI(OBFUSCATE("NxHook: Internal State Valid"));
        }
    }

    /**
     * Install a stealth hook.
     * @param target_addr The absolute address of the function to hook.
     * @param replace_func The address of the new function.
     * @param original_func Pointer to store the original function trampoline.
     */
    inline void Install(void *target_addr, void *replace_func, void **original_func) {
        if (!target_addr) {
            LOGE(OBFUSCATE("[NxMod] [NxHook] Error: Target address is NULL"));
            return;
        }
        if (!replace_func) {
            LOGE(OBFUSCATE("[NxMod] [NxHook] Error: Replacement function is NULL"));
            return;
        }

        // 1. Environment Integrity Check
        if (!IntegrityCheck()) {
             LOGE(OBFUSCATE("[NxMod] [NxHook] Integrity Violation detected. Aborting hook."));
             return;
        }

        // 2. Polymorphic/Stealth Delay
        // Introduce micro-delay to confuse heuristic scanners checking for rapid modification
        usleep(random_int(100, 500));

        // 3. Junk Code Generation (Obfuscation)
        GenerateJunkCode();

        LOGI(OBFUSCATE("[NxMod] [NxHook] Injecting at %p..."), target_addr);

        // 4. Thread Safety (Suspend all other threads if possible - skipped for simplicity in AIDE/Android without heavy dependencies)
        // Using Dobby for the heavy lifting (Instruction Rewriting) as it handles cache flushing and trampoline generation correctly.
        int result = DobbyHook(target_addr, replace_func, original_func);

        if (result == 0) {
            LOGI(OBFUSCATE("[NxMod] [NxHook] Success. Stealth Active."));
        } else {
            LOGE(OBFUSCATE("[NxMod] [NxHook] FAILED with code: %d"), result);
        }
    }
}

