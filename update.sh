sed -i '363,410c\
        // Category Icon in rounded container\
        Box(\
            modifier = Modifier\
                .size(48.dp)\
                .clip(RoundedCornerShape(12.dp))\
                .background(\
                    if (isSelected) category.accentColor.copy(alpha = 0.20f)\
                    else Color.Transparent\
                )\
                .border(\
                    width = 1.dp,\
                    color = if (isSelected) category.accentColor else Color(0x66FFFFFF),\
                    shape = RoundedCornerShape(12.dp)\
                ),\
            contentAlignment = Alignment.Center\
        ) {\
            Icon(\
                imageVector = category.icon,\
                contentDescription = if (language == Language.TH) category.titleTh else category.titleEn,\
                tint = if (isSelected) category.accentColor else Color.White,\
                modifier = Modifier.size(24.dp)\
            )\
        }\
        Spacer(modifier = Modifier.height(8.dp))\
        // Title\
        Text(\
            text = if (language == Language.TH) category.titleTh else category.titleEn,\
            color = if (isSelected) category.accentColor else Color(0xFFD8E4FC),\
            fontSize = 12.sp,\
            fontWeight = FontWeight.Medium,\
            textAlign = TextAlign.Center,\
            maxLines = 1\
        )\
    }\
}\
' app/src/main/java/com/example/ui/components/ExperienceAssessmentView.kt
bash update.sh
