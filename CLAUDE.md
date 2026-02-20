# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**SDS MES (Manufacturing Execution System)** - A modular, base MES platform designed for customization across various manufacturing industries (chemical, electrical, electronic factories, etc.).

**Developer**: 문명섭 (Moon Myeong-seop)
**Company**: (주)스마트도킹스테이션 (SmartDockingStation Co., Ltd.)
**Contact**: msmoon.asi@gmail.com | 010-4882-2035
**Project Root**: D:\prj\smartdocking\prj\claude\SDMES

## Critical Development Principles

### User Interaction Requirements
1. **Always ask before making changes** that are not explicitly requested by the user
2. **Propose options** when there are multiple good approaches - let the user decide
3. **Explain logic changes** in detail when implementing features
4. **Never remove or modify features** without explicit user approval and discussion

### Conversation Logging
- Automatically save conversation logs to a separate folder
- Verify log saving is working before proceeding with other tasks
- Do not ask the user about this - it should happen automatically

### Project-Specific Requirements
- **Date/Time Format**: Always use 24-hour format
- **Database Table Prefix**: All tables must use `SI_` prefix (e.g., `SI_Orders`, `SI_Products`)
- **Database Schema**: Always optimize for performance
- **prd.txt**: Monitor this file continuously - it is updated regularly with evolving requirements

### Design Philosophy
- **Visual Style**: Professional, sophisticated, refined, and premium appearance suitable for industrial manufacturing environments
- **Base MES Concept**: Design for modularity and easy customization across different manufacturing sectors
- **Configuration-Driven**: Enable rapid customization through settings/configuration rather than code changes to minimize deployment and customization time

## Architecture Goals

### Modular Base Platform
The system should be architected as a **base MES platform** that can be rapidly customized for:
- Chemical manufacturing plants
- Electrical/electronic factories
- Various other manufacturing industries

### Customization Strategy
- **Environment Configuration**: Industry-specific settings accessible through configuration files/UI
- **Plugin Architecture**: Sector-specific modules that extend base functionality
- **Minimal Code Changes**: Customization should primarily use configuration, not code modification
- **Fast Deployment**: Minimize time from base platform to customer-specific solution

## Reference Documents

### Key Planning Documents
- `MES개발_2안_화면설계서.pdf` - UI/Screen design specification (Design Plan 2)
- `2안_MES_기능대비표_목록체크_20250108.pdf` - Feature comparison checklist
- `MES(1안)_작업계획_20250324.xlsx` - Work plan (Plan 1)
- `prd.txt` - Living requirements document (continuously updated)

### Project Timeline Considerations
When planning development, consider:
- Project start date
- Contract date
- Interim completion date
- Integration testing date
- Final delivery date

## Development Workflow

### When Implementing Features
1. Read and understand relevant planning documents
2. Propose implementation approach with options
3. Wait for user approval before proceeding
4. Implement with detailed explanations of logic
5. Consider base MES modularity in design decisions

### Tool Installation
- Ask before installing any new tools or dependencies
- Propose options when multiple tools could solve the problem

### Code Structure
Since this is a greenfield project, establish patterns early that support:
- Industry-agnostic core functionality
- Industry-specific extension points
- Configuration-driven behavior
- Database schema optimization with `SI_` table naming convention

## Next Steps for Initial Development

When code development begins, this file should be updated with:
- Build commands and development environment setup
- Testing commands and framework information
- Specific architecture patterns chosen (framework, database, etc.)
- Module structure and dependency relationships
- Configuration system architecture
